package edu.stanford.protege.versioning.services.storage;

import edu.stanford.protege.versioning.services.storage.dtos.ProjectBackupFiles;
import edu.stanford.protege.webprotege.csv.DocumentId;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {

    Path downloadFile(DocumentId location);

    Path extractBackupFiles(Path downloadedFiles) throws IOException;

    void cleanUpFiles(Path pathToFiles) throws IOException;

    ProjectBackupFiles getProjectBackupFilesFromPath(Path backupFilesDirectory);
}
