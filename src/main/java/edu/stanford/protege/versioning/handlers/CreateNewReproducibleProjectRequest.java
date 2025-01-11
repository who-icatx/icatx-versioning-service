package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

@JsonTypeName(CreateNewReproducibleProjectRequest.CHANNEL)
public record CreateNewReproducibleProjectRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("branch") String branch
)implements Request<CreateNewReproducibleProjectResponse> {
    public static final String CHANNEL = "icatx.versioning.CreateNewReproducibleProjectRequest";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
