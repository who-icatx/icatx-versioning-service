package edu.stanford.protege.versioning.services.storage.project;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.revision.ChangeHistoryFileFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ChangeHistoryFileProvider {

    @Nonnull
    private final ChangeHistoryFileFactory changeHistoryFileFactory;

    public ChangeHistoryFileProvider(@Nonnull ChangeHistoryFileFactory changeHistoryFileFactory) {
        this.changeHistoryFileFactory = checkNotNull(changeHistoryFileFactory);
    }

    public File get(ProjectId projectId) {
        return changeHistoryFileFactory.getChangeHistoryFile(projectId);
    }
}
