package edu.stanford.protege.versioning.services;

import edu.stanford.protege.versioning.dtos.RegularTempFile;
import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
import edu.stanford.protege.versioning.services.email.MailgunApiService;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.versioning.services.storage.StorageService;
import edu.stanford.protege.versioning.projects.SetProjectUnderMaintenanceAction;
import edu.stanford.protege.versioning.projects.SetProjectUnderMaintenanceResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Service
public class ProjectBackupService {


    private final static Logger LOGGER = LoggerFactory.getLogger(ProjectBackupService.class);

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

    @Autowired
    private CommandExecutor<SetProjectUnderMaintenanceAction, SetProjectUnderMaintenanceResult> setProjectUnderMaintenanceExecutor;

    @Value("${webprotege.versioning.location}")
    private String smallGitFilePrefixLocation;

    private static final int MAINTENANCE_RETRY_COUNT = 3;
    private static final int MAINTENANCE_TIMEOUT_BASE_SEC = 5;
    private static final int MAINTENANCE_TIMEOUT_MAX_SEC = 10;

    private void executeSetMaintenanceWithRetry(ProjectId project, ExecutionContext executionContext, boolean underMaintenance, String actionLabel) {
        Exception lastException = null;
        for (int attempt = 0; attempt <= MAINTENANCE_RETRY_COUNT; attempt++) {
            int timeoutSec = Math.min(MAINTENANCE_TIMEOUT_BASE_SEC + attempt, MAINTENANCE_TIMEOUT_MAX_SEC);
            try {
                SetProjectUnderMaintenanceAction action = SetProjectUnderMaintenanceAction.create(project, underMaintenance);
                setProjectUnderMaintenanceExecutor.execute(action, executionContext).get(timeoutSec, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                lastException = e;
                LOGGER.warn("{} (attempt {}/{}), timeout {}s failed for project {}", actionLabel, attempt + 1, MAINTENANCE_RETRY_COUNT + 1, timeoutSec, project.id(), e);
                if (attempt == MAINTENANCE_RETRY_COUNT) {
                    throw new RuntimeException(actionLabel + " failed after " + (MAINTENANCE_RETRY_COUNT + 1) + " attempts", lastException);
                }
            }
        }
    }

    public List<IRI> createBackup(String projectId, ExecutionContext executionContext) {
        LOGGER.info("Starting create backup flow for project " + projectId + "with execution context " + executionContext);
        ProjectId project = ProjectId.valueOf(projectId);
        List<IRI> allChangedEntities = service.getAllChangedEntitiesSinceLastBackupDate(project, executionContext);
        ReproducibleProject reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId);

        if(allChangedEntities.isEmpty()){
            LOGGER.info("Project " + projectId + " has no changed entties since last backup date. Skipping backup");
            return new ArrayList<>();
        } else {
            LOGGER.info("Creating backup for " + allChangedEntities.size() + " entities on projectId " + projectId);
        }

        if (reproducibleProject == null) {
            throw new RuntimeException("Project not found " + projectId);
        }

        LOGGER.info("Setting project {} under maintenance before backup", projectId);
        executeSetMaintenanceWithRetry(project, executionContext, true, "Set project under maintenance before backup");
        LOGGER.info("Project {} set under maintenance successfully", projectId);

        try {
            gitService.gitCheckout(reproducibleProject.getAssociatedBranch(), smallGitFilePrefixLocation + projectId);

            CompletableFuture<String> backupOwlBinaryTask = service.makeBackupForOwlBinaryFile(project, executionContext);
            CompletableFuture<RegularTempFile> collectionsBackupTask = CompletableFuture.runAsync(() -> backupFilesProcessor.dumpMongoDb())
                    .thenApply(result -> backupFilesProcessor.createCollectionsBackup(project));
            CompletableFuture<Void> writeChangedEntities = CompletableFuture.runAsync(() -> service.saveEntitiesSinceLastBackupDate(project, allChangedEntities, reproducibleProject, executionContext));

            CompletableFuture.allOf(backupOwlBinaryTask, collectionsBackupTask, writeChangedEntities).join();
            RegularTempFile owlBinary = RegularTempFile.create(backupOwlBinaryTask.get());
            RegularTempFile mongoCollections = collectionsBackupTask.get();


            String commitMessage = String.join(", ", allChangedEntities.stream().map(IRI::toString).toList());

            Path finalBackupFilesArchive = storageService.combineFilesIntoArchive(backupDirectoryProvider.get(project), mongoCollections, owlBinary);


            gitService.commitAndPushChanges(versioningDirectoryProvider.get(project).toAbsolutePath().toString(), finalBackupFilesArchive.toAbsolutePath().toString(), commitMessage);


            mongoCollections.clearTempFiles();
            return allChangedEntities;
        } catch (Exception e) {
            mailgunApiService.sendMail(e);
            throw new RuntimeException("Error during backup", e);
        } finally {
            try {
                LOGGER.info("Removing maintenance mode for project {} after backup", projectId);
                executeSetMaintenanceWithRetry(project, executionContext, false, "Remove maintenance mode after backup");
                LOGGER.info("Project {} maintenance mode removed successfully", projectId);
            } catch (Exception e) {
                LOGGER.error("Failed to remove maintenance mode for project {} after backup", projectId, e);
            }
        }
    }
}
