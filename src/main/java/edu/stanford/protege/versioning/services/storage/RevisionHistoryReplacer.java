package edu.stanford.protege.versioning.services.storage;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.revision.ChangeHistoryFileFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-07
 */
@Component
public class RevisionHistoryReplacer {

    private final ChangeHistoryFileFactory changeHistoryFileFactory;

    public RevisionHistoryReplacer(ChangeHistoryFileFactory changeHistoryFileFactory) {
        this.changeHistoryFileFactory = changeHistoryFileFactory;
    }

    public void replaceRevisionHistory(ProjectId projectId, Path revisionHistory) {

        try {
            var projectHistoryFile = changeHistoryFileFactory.getChangeHistoryFile(projectId).toPath();
            Files.createDirectories(projectHistoryFile.getParent());
            Files.move(revisionHistory, projectHistoryFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
