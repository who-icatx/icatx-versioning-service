package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/versioning-commands")
public class VersioningCommandsController {


    @Autowired
    private OwlClassesService service;

    @PostMapping(value = {"/{projectId}/initial-files"})
    public ResponseEntity<List<IRI>> createInitialFiles(@PathVariable String projectId) throws ExecutionException, InterruptedException {
        List<IRI> savedIris = service.saveInitialLoad(ProjectId.valueOf(projectId));
        return ResponseEntity.ok(savedIris);
    }
}
