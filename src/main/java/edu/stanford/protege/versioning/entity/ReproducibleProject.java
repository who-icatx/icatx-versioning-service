package edu.stanford.protege.versioning.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "ReproducibleProjects")
public class ReproducibleProject {

    @Id
    private String id;
    @Field("projectId")
    private String projectId;
    @Field("lastBackup")
    private long lastBackupTimestamp;

    @Field("associatedBranch")
    private String associatedBranch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public long getLastBackupTimestamp() {
        return lastBackupTimestamp;
    }


    public void setLastBackupTimestamp(long lastBackupTimestamp) {
        this.lastBackupTimestamp = lastBackupTimestamp;
    }

    public String getAssociatedBranch() {
        return associatedBranch;
    }

    public void setAssociatedBranch(String associatedBranch) {
        this.associatedBranch = associatedBranch;
    }
}
