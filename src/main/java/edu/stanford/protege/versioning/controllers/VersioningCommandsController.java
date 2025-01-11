package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.services.*;
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
    private ProjectBackupService backupService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ProjectBackupDirectoryProvider backupDirectoryProvider;

    @Autowired
    private ProjectVersioningDirectoryProvider versioningDirectoryProvider;

    @Autowired
    private ReproducibleProjectsRepository reproducibleProjectsRepository;

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
        var savedIris = backupService.createBackup(projectId);
        return ResponseEntity.ok(savedIris);
    }

}
