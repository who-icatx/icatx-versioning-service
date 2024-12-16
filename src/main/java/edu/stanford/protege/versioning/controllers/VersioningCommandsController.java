package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.dtos.MongoCollectionsTempFiles;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.services.ProjectBackupDirectoryProvider;
import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
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
        ProjectBackupDirectoryProvider backupDirectoryProvider = new ProjectBackupDirectoryProvider(project);

        CompletableFuture<List<IRI>> saveEntitiesTask = CompletableFuture.supplyAsync(() -> service.saveEntitiesSinceLastBackupDate(project));
        CompletableFuture<String> backupOwlBinaryTask = CompletableFuture.supplyAsync(() -> service.makeBackupForOwlBinaryFile(project));
        CompletableFuture<Void> dumpMongoTask = CompletableFuture.runAsync(() -> backupService.dumpMongoDb());
        CompletableFuture<MongoCollectionsTempFiles> createCollectionsBackupTask = CompletableFuture.supplyAsync(() -> backupService.createCollectionsBackup(project));

        try {
            CompletableFuture.allOf(saveEntitiesTask, backupOwlBinaryTask, dumpMongoTask, createCollectionsBackupTask).join();

            List<IRI> updatedIris = saveEntitiesTask.join();
            String pathToOwlBinary = backupOwlBinaryTask.join();
            MongoCollectionsTempFiles mongoCollectionsTempFiles = createCollectionsBackupTask.join();

            // Add owlBinary to the MongoDB collections archive
            Path finalBackupFilesArchive = storageService.combineFilesIntoArchive(mongoCollectionsTempFiles, pathToOwlBinary, project, backupDirectoryProvider.get().toPath());

            mongoCollectionsTempFiles.clearTempFiles();

            return ResponseEntity.ok(updatedIris);

        } catch (Exception e) {
            throw new RuntimeException("Error during backup", e);
        }
    }

}
