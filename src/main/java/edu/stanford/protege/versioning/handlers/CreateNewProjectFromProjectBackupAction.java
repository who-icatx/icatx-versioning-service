package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Objects;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.dispatch.Action;
import edu.stanford.protege.webprotege.project.NewProjectSettings;

import static com.google.common.base.MoreObjects.toStringHelper;
import static edu.stanford.protege.versioning.handlers.CreateNewProjectFromProjectBackupAction.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateNewProjectFromProjectBackupAction(
        @JsonProperty("projectId") ProjectId newProjectId,
        @JsonProperty("newProjectSettings") NewProjectSettings newProjectSettings
) implements Action<CreateNewProjectFromProjectBackupResult> {

    public static final String CHANNEL = "webprotege.projects.CreateNewProjectFromProjectBackup";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(newProjectId, newProjectSettings);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CreateNewProjectFromProjectBackupAction)) {
            return false;
        }
        CreateNewProjectFromProjectBackupAction other = (CreateNewProjectFromProjectBackupAction) obj;
        return this.newProjectId.equals(other.newProjectId) && this.newProjectSettings.equals(other.newProjectSettings);
    }

    @Override
    public String toString() {
        return toStringHelper("CreateNewProjectFromProjectBackupAction")
                .addValue(newProjectId)
                .addValue(newProjectSettings)
                .toString();
    }


}
