package edu.stanford.protege.versioning.services.python;

import edu.stanford.protege.versioning.dtos.MongoCollectionsTempFiles;
import edu.stanford.protege.webprotege.common.ProjectId;

import java.nio.file.Path;

public interface PythonService {
    void importMongoCollections(ProjectId projectId, Path inputDirectory);

    void createMongoDump();

    MongoCollectionsTempFiles createCollectionsBackup(ProjectId projectId);
}
