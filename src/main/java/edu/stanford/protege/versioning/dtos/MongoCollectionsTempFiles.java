package edu.stanford.protege.versioning.dtos;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;

public record MongoCollectionsTempFiles(Path baseDirectory) implements TempFiles {

    @Override
    public void clearTempFiles() throws IOException {
        if (Files.exists(baseDirectory)) {
            FileUtils.forceDelete(baseDirectory.toFile());
        }
    }
}
