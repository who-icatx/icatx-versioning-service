package edu.stanford.protege.versioning.projects;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.MoreObjects;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class SetProjectUnderMaintenanceAction implements Request<SetProjectUnderMaintenanceResult> {

    public static final String CHANNEL = "webprotege.projects.SetProjectUnderMaintenance";

    private ProjectId projectId;

    private boolean underMaintenance;

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    private SetProjectUnderMaintenanceAction(ProjectId projectId, boolean underMaintenance) {
        this.projectId = checkNotNull(projectId);
        this.underMaintenance = underMaintenance;
    }

    public SetProjectUnderMaintenanceAction() {
    }

    @JsonCreator
    public static SetProjectUnderMaintenanceAction create(@JsonProperty("projectId") ProjectId projectId,
                                                          @JsonProperty("underMaintenance") boolean underMaintenance) {
        return new SetProjectUnderMaintenanceAction(projectId, underMaintenance);
    }

    @Nonnull
    public ProjectId projectId() {
        return projectId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, underMaintenance);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SetProjectUnderMaintenanceAction)) {
            return false;
        }
        SetProjectUnderMaintenanceAction other = (SetProjectUnderMaintenanceAction) obj;
        return this.projectId.equals(other.projectId) && this.underMaintenance == other.underMaintenance;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("SetProjectUnderMaintenanceAction")
                          .add("projectId", projectId)
                          .add("underMaintenance", underMaintenance)
                          .toString();
    }
}

