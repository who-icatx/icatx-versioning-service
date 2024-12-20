package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.dtos.RegularTempFile;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.services.*;
import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
import edu.stanford.protege.versioning.services.email.MailgunApiService;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.versioning.services.storage.StorageService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

@RestController
@RequestMapping("/versioning-commands")
public class VersioningCommandsController {


    @Autowired
    private OwlClassesService service;

    @Autowired
    private BackupFilesProcessor backupService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ProjectBackupDirectoryProvider backupDirectoryProvider;

    @Autowired
    private ProjectVersioningDirectoryProvider versioningDirectoryProvider;

    @Autowired
    private GitService gitService;

    @Autowired
    private MailgunApiService mailgunApiService;

    @PostMapping(value = {"/{projectId}/initial-files"})
    public ResponseEntity<List<IRI>> createInitialFiles(@PathVariable String projectId) throws ExecutionException, InterruptedException {
        List<IRI> savedIris = service.saveInitialOntologyInfo(ProjectId.valueOf(projectId));
        return ResponseEntity.ok(savedIris);
    }

    @GetMapping(value = {"/{projectId}/save-changed-entities"})
    public ResponseEntity<List<IRI>> testSaveChangedEntities(@PathVariable String projectId) {
        List<IRI> updatedIris = service.saveEntitiesSinceLastBackupDate(ProjectId.valueOf(projectId));
        return ResponseEntity.ok(updatedIris);
    }

    @PostMapping(value = {"/{projectId}/create-backup"})
    public ResponseEntity<List<IRI>> createBackup(@PathVariable String projectId) {
        ProjectId project = ProjectId.valueOf(projectId);

        CompletableFuture<String> backupOwlBinaryTask = service.makeBackupForOwlBinaryFile(project);
        CompletableFuture<RegularTempFile> collectionsBackupTask = CompletableFuture.runAsync(() -> backupService.dumpMongoDb())
                .thenApply(result -> backupService.createCollectionsBackup(project));

        try {
            CompletableFuture.allOf(backupOwlBinaryTask, collectionsBackupTask).join();
            RegularTempFile owlBinary = RegularTempFile.create(backupOwlBinaryTask.join());
            RegularTempFile mongoCollections = collectionsBackupTask.join();


            List<IRI> saveEntitiesTask = service.saveEntitiesSinceLastBackupDate(project);

            String commitMessage = String.join(", ", saveEntitiesTask.stream().map(IRI::toString).toList());

            Path finalBackupFilesArchive = storageService.combineFilesIntoArchive(backupDirectoryProvider.get(project), mongoCollections, owlBinary);


            gitService.commitAndPushChanges(versioningDirectoryProvider.get(project).toAbsolutePath().toString(), finalBackupFilesArchive.toAbsolutePath().toString(), commitMessage);


            mongoCollections.clearTempFiles();


            return ResponseEntity.ok(saveEntitiesTask);

        } catch (Exception e) {
            mailgunApiService.sendMail(e);
            throw new RuntimeException("Error during backup", e);
        }
    }

}
