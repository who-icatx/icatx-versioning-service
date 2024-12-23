package edu.stanford.protege.versioning.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ProjectVersioningDirectoryProvider {

    @Value("${webprotege.versioning.location}")
    private Path backupDirectory;


    public Path get(ProjectId projectId) {
        return backupDirectory.resolve(projectId.id());
    }
}