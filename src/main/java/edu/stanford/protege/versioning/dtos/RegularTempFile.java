package edu.stanford.protege.versioning.dtos;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;

public record RegularTempFile(Path baseDirectory) implements TempFiles {

    public static RegularTempFile create(Path baseDirectory){
        return new RegularTempFile(baseDirectory);
    }

    public static RegularTempFile create(String baseDirectory){
        return new RegularTempFile(Paths.get(baseDirectory));
    }

    @Override
    public void clearTempFiles() throws IOException {
        if (Files.exists(baseDirectory)) {
            FileUtils.forceDelete(baseDirectory.toFile());
        }
    }
}
