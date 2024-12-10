package edu.stanford.protege.versioning.services.backupProcessor;

import edu.stanford.protege.versioning.services.storage.dtos.DocumentId;
import edu.stanford.protege.webprotege.common.ProjectId;

public interface BackupFilesProcessor {

    void processBackupFiles(ProjectId projectId, DocumentId documentId);
}
