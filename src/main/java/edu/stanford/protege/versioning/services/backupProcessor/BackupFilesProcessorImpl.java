package edu.stanford.protege.versioning.services.backupProcessor;

import edu.stanford.protege.versioning.BackupFileProcessingException;
import edu.stanford.protege.versioning.services.python.PythonService;
import edu.stanford.protege.versioning.services.storage.*;
import edu.stanford.protege.versioning.services.storage.dtos.*;
import edu.stanford.protege.webprotege.common.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BackupFilesProcessorImpl implements BackupFilesProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BackupFilesProcessorImpl.class);


    private final PythonService pythonService;
    private final StorageService storageService;


    public BackupFilesProcessorImpl(PythonService pythonService,
                                    StorageService storageService) {
        this.pythonService = pythonService;
        this.storageService = storageService;
    }

    @Override
    public BlobLocation prepareOwlBinaryAndImportCollections(ProjectId projectId, DocumentId documentId) {
        var downloadedFiles = storageService.downloadFile(documentId);
        try {
            var backupFiles = storageService.extractBackupFiles(downloadedFiles);
            ProjectBackupFiles projectBackupFiles = storageService.getProjectBackupFilesFromPath(backupFiles);
            BlobLocation owlBinaryLocation = storageService.uploadFileToMinio(projectBackupFiles.owlBinaryFile().toPath());
            pythonService.importMongoCollections(projectId, backupFiles);
            storageService.cleanUpFiles(backupFiles);
            return owlBinaryLocation;
        } catch (IOException e) {
            String message = "Error while preparing backup files for use";
            logger.error(message, e);
            throw new BackupFileProcessingException(message, e);
        }
        finally {
            try {
                storageService.cleanUpFiles(downloadedFiles);
            } catch (IOException e) {
                String message = "Error while trying to cleanup backup files";
                logger.error(message, e);
            }
        }
    }
}
