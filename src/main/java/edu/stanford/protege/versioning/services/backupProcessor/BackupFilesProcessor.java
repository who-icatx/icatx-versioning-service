package edu.stanford.protege.versioning.services.backupProcessor;

import edu.stanford.protege.versioning.services.storage.dtos.DocumentId;
import edu.stanford.protege.webprotege.common.*;

public interface BackupFilesProcessor {

    BlobLocation prepareOwlBinaryAndImportCollections(ProjectId projectId, DocumentId documentId);
}
