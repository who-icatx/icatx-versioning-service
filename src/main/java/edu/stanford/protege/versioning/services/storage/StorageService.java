package edu.stanford.protege.versioning.services.storage;


import edu.stanford.protege.versioning.dtos.*;
import edu.stanford.protege.webprotege.common.*;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {

    Path downloadFile(DocumentId location);

    Path extractBackupFiles(Path downloadedFiles) throws IOException;

    void cleanUpFiles(Path pathToFiles) throws IOException;

    ProjectBackupFiles getProjectBackupFilesFromPath(Path backupFilesDirectory);

    BlobLocation uploadFileToMinio(Path fileToStore) throws IOException;

    Path combineFilesIntoArchive(Path outputPath, RegularTempFile... regularTempFiles);

    void zipDirectory(Path sourceDir, Path archivePath) throws IOException;

    RegularTempFile createFile(Path directory, String fileName) throws IOException;
}
