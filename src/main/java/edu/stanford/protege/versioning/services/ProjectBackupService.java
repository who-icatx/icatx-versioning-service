package edu.stanford.protege.versioning.services;

import edu.stanford.protege.versioning.dtos.RegularTempFile;
import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
import edu.stanford.protege.versioning.services.email.MailgunApiService;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.versioning.services.storage.StorageService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;



@Service
public class ProjectBackupService {
    @Autowired
    private OwlClassesService service;

    @Autowired
    private BackupFilesProcessor backupFilesProcessor;

    @Autowired
    private ReproducibleProjectsRepository reproducibleProjectsRepository;

    @Autowired
    private GitService gitService;

    @Autowired
    private ProjectBackupDirectoryProvider backupDirectoryProvider;

    @Autowired
    private ProjectVersioningDirectoryProvider versioningDirectoryProvider;

    @Autowired
    private StorageService storageService;

    @Autowired
    private MailgunApiService mailgunApiService;



    public List<IRI> createBackup(String projectId) {
        ProjectId project = ProjectId.valueOf(projectId);

        CompletableFuture<String> backupOwlBinaryTask = service.makeBackupForOwlBinaryFile(project);
        CompletableFuture<RegularTempFile> collectionsBackupTask = CompletableFuture.runAsync(() -> backupFilesProcessor.dumpMongoDb())
                .thenApply(result -> backupFilesProcessor.createCollectionsBackup(project));

        ReproducibleProject reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId);

        if (reproducibleProject == null) {
            throw new RuntimeException("Project not found " + projectId);
        }

        CompletableFuture<Void> createGitRepo = CompletableFuture.runAsync(() -> gitService.gitCheckout(reproducibleProject.getAssociatedBranch(), "/srv/versioning/" + projectId));


        try {
            CompletableFuture.allOf(backupOwlBinaryTask, collectionsBackupTask, createGitRepo).join();
            RegularTempFile owlBinary = RegularTempFile.create(backupOwlBinaryTask.get());
            RegularTempFile mongoCollections = collectionsBackupTask.get();

            List<IRI> saveEntitiesTask = service.saveEntitiesSinceLastBackupDate(project);

            String commitMessage = String.join(", ", saveEntitiesTask.stream().map(IRI::toString).toList());

            Path finalBackupFilesArchive = storageService.combineFilesIntoArchive(backupDirectoryProvider.get(project), mongoCollections, owlBinary);


            gitService.commitAndPushChanges(versioningDirectoryProvider.get(project).toAbsolutePath().toString(), finalBackupFilesArchive.toAbsolutePath().toString(), commitMessage);


            mongoCollections.clearTempFiles();
            return saveEntitiesTask;
        } catch (Exception e) {
            mailgunApiService.sendMail(e);
            throw new RuntimeException("Error during backup", e);
        }
    }
}
