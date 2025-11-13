package edu.stanford.protege.versioning.projects;

import com.google.common.base.MoreObjects;
import edu.stanford.protege.webprotege.common.Response;

import java.util.Objects;

public class SetProjectUnderMaintenanceResult implements Response {

    private boolean underMaintenance;

    private SetProjectUnderMaintenanceResult() {
    }

    private SetProjectUnderMaintenanceResult(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    public static SetProjectUnderMaintenanceResult create(boolean underMaintenance) {
        return new SetProjectUnderMaintenanceResult(underMaintenance);
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(underMaintenance);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SetProjectUnderMaintenanceResult)) {
            return false;
        }
        SetProjectUnderMaintenanceResult other = (SetProjectUnderMaintenanceResult) obj;
        return this.isUnderMaintenance() == other.isUnderMaintenance();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("SetProjectUnderMaintenanceResult")
                          .addValue(underMaintenance)
                          .toString();
    }
}

