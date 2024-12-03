package edu.stanford.protege.versioning.controllers;


import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/versioning-commands")
public class VersioningCommandsController {


    @Autowired
    private OwlClassesService service;

    @GetMapping(value = {"/{projectId}"})
    public ResponseEntity<String> testMethod(@PathVariable String projectId) throws ExecutionException, InterruptedException {
        service.getAllClasses(ProjectId.valueOf(projectId));
        return ResponseEntity.ok("This is okay");
    }
}
