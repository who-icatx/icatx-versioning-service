package edu.stanford.protege.versioning.services.backupProcessor;

import edu.stanford.protege.versioning.dtos.*;
import edu.stanford.protege.versioning.handlers.PrepareBackupFilesForUseResponse;
import edu.stanford.protege.webprotege.common.*;

public interface BackupFilesProcessor {

    PrepareBackupFilesForUseResponse prepareOwlBinaryAndImportCollections(ProjectId projectId, DocumentId documentId);

    void dumpMongoDb();

    RegularTempFile createCollectionsBackup(ProjectId projectId);
}
