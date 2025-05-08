package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.services.*;
import edu.stanford.protege.versioning.services.email.MailgunApiService;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.versioning.services.storage.StorageService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("/versioning-commands")
public class VersioningCommandsController {


    @Autowired
    private OwlClassesService service;

    @Autowired
    private ProjectBackupService backupService;


    @PostMapping(value = {"/{projectId}/init-entity-files"})
    public ResponseEntity<List<IRI>> createInitialFiles(@PathVariable String projectId) throws ExecutionException, InterruptedException {
        List<IRI> savedIris = service.saveInitialOntologyInfo(ProjectId.valueOf(projectId));
        return ResponseEntity.ok(savedIris);
    }

    @PostMapping(value = {"/{projectId}/backup"})
    public ResponseEntity<List<IRI>> createBackup(@PathVariable String projectId) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        var savedIris = backupService.createBackup(projectId);
        return ResponseEntity.ok(savedIris);
    }

}
