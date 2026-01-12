package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.SecurityContextHelper;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.services.*;
import edu.stanford.protege.versioning.projects.SetProjectUnderMaintenanceAction;
import edu.stanford.protege.versioning.projects.SetProjectUnderMaintenanceResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @Autowired
    private ProjectBackupScheduler scheduler;

    @Autowired
    private CommandExecutor<SetProjectUnderMaintenanceAction, SetProjectUnderMaintenanceResult> setProjectUnderMaintenanceExecutor;

    @PostMapping(value = {"/{projectId}/init-entity-files"})
    public ResponseEntity<List<IRI>> createInitialFiles(@PathVariable String projectId) throws ExecutionException, InterruptedException {
        List<IRI> savedIris = service.saveInitialOntologyInfo(ProjectId.valueOf(projectId), SecurityContextHelper.getExecutionContext());
        return ResponseEntity.ok(savedIris);
    }

    @PostMapping(value = {"/{projectId}/init-children-files"})
    public ResponseEntity<String> createInitialChildrenFiles(@PathVariable String projectId) throws ExecutionException, InterruptedException, TimeoutException {
        service.initialEntitiesChildrenSave(ProjectId.valueOf(projectId), SecurityContextHelper.getExecutionContext());
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value = {"/{projectId}/backup"})
    public ResponseEntity<List<IRI>> createBackup(@PathVariable String projectId) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        var savedIris = backupService.createBackup(projectId, SecurityContextHelper.getExecutionContext());
        return ResponseEntity.ok(savedIris);
    }

    @PostMapping(value = {"/{projectId}/set-maintenance"})
    public ResponseEntity<Boolean> setProjectUnderMaintenance(@PathVariable String projectId, 
                                                              @RequestParam boolean underMaintenance) 
            throws ExecutionException, InterruptedException, TimeoutException {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        
        ProjectId project = ProjectId.valueOf(projectId);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
        
        SetProjectUnderMaintenanceAction action = SetProjectUnderMaintenanceAction.create(project, underMaintenance);
        SetProjectUnderMaintenanceResult result = setProjectUnderMaintenanceExecutor.execute(action, executionContext)
                .get(5, TimeUnit.SECONDS);
        
        return ResponseEntity.ok(result.isUnderMaintenance());
    }

}
