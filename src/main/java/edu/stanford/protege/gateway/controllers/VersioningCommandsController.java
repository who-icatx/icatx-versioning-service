package edu.stanford.protege.gateway.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/versioning-commands")
public class VersioningCommandsController {


    @GetMapping
    public ResponseEntity<String> testMethod(){
        return ResponseEntity.ok("This is okay");
    }
}
