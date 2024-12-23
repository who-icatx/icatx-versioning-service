package edu.stanford.protege.versioning.history;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

import java.sql.Timestamp;

@JsonTypeName(GetChangedEntitiesRequest.CHANNEL)
public record GetChangedEntitiesRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("timestamp") long timestamp
) implements Request<GetChangedEntitiesResponse> {

    public static final String CHANNEL = "webprotege.history.GetChangedEntities";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static GetChangedEntitiesRequest create(ProjectId projectId,
                                                   long timestamp) {
        return new GetChangedEntitiesRequest(projectId, timestamp);
    }
}
