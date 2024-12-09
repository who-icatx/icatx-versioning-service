package edu.stanford.protege.versioning.services.storage;

import org.springframework.beans.factory.annotation.Value;

import javax.inject.*;
import java.io.File;
import java.nio.file.Path;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 06/02/15
 */
public class DataDirectoryProvider implements Provider<File> {

    @Value("${webprotege.directories.data}")
    private Path dataDirectoryPath;

    @Inject
    public DataDirectoryProvider() {
    }

    @Override
    public File get() {
        return dataDirectoryPath.toFile();
    }
}
