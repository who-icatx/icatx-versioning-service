package edu.stanford.protege.versioning.services.storage;


import edu.stanford.protege.versioning.services.storage.dtos.*;
import edu.stanford.protege.webprotege.common.BlobLocation;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {

    Path downloadFile(DocumentId location);

    Path extractBackupFiles(Path downloadedFiles) throws IOException;

    void cleanUpFiles(Path pathToFiles) throws IOException;

    ProjectBackupFiles getProjectBackupFilesFromPath(Path backupFilesDirectory);

    BlobLocation uploadFileToMinio(Path fileToStore) throws IOException;
}
