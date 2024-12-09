package edu.stanford.protege.versioning.services.backupProcessor;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.csv.DocumentId;

public interface BackupFilesProcessor {

    void processBackupFiles(ProjectId projectId, DocumentId documentId);
}
