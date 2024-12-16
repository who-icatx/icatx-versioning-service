package edu.stanford.protege.versioning.dtos;

import java.io.File;
import java.util.List;

public record ProjectBackupFiles(File owlBinaryFile, List<File> mongoCollections) {
}
