package edu.stanford.protege.versioning.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Path;

public class ProjectBackupDirectoryProvider {

    @Value("${webprotege.directories.backup}")
    private Path backupDirectory;

    private final ProjectId projectId;

    public ProjectBackupDirectoryProvider(ProjectId projectId) {
        this.projectId = projectId;
    }


    public File get() {
        return new File(backupDirectory.toFile(), projectId.id());
    }
}