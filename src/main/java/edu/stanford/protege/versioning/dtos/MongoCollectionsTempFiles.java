package edu.stanford.protege.versioning.dtos;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;

public class MongoCollectionsTempFiles implements TempFiles {

    private final Path baseDirectory;

    public MongoCollectionsTempFiles(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void clearTempFiles() throws IOException {
        if (Files.exists(baseDirectory)) {
            FileUtils.forceDelete(baseDirectory.toFile());
        }
    }

    public Path getBaseDirectory(){
        return baseDirectory;
    }
}
