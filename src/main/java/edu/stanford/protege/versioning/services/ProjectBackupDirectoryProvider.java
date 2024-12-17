package edu.stanford.protege.versioning.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ProjectBackupDirectoryProvider {

    @Value("${webprotege.directories.backup}")
    private Path backupDirectory;


    public Path get(ProjectId projectId) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return backupDirectory.resolve(projectId.id()).resolve(currentDate);
    }
}