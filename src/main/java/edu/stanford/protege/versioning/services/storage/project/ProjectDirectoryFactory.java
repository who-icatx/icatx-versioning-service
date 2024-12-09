package edu.stanford.protege.versioning.services.storage.project;

import edu.stanford.protege.versioning.services.storage.DataDirectory;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23/02/16
 */
@Component
public class ProjectDirectoryFactory {

    private final File dataDirectory;

    @Inject
    public ProjectDirectoryFactory(@DataDirectory File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public File getProjectDirectory(ProjectId projectId) {
        return new File(getProjectDataDirectory(), projectId.id());
    }

    private File getProjectDataDirectory() {
        return new File(getDataStoreDirectory(), "project-data");
    }

    private File getDataStoreDirectory() {
        return new File(dataDirectory, "data-store");
    }

}
