package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

@JsonTypeName(CreateProjectSmallFilesRequest.CHANNEL)
public record CreateProjectSmallFilesRequest(
        @JsonProperty("projectId") ProjectId projectId
)implements Request<CreateProjectSmallFilesResponse> {
    public static final String CHANNEL = "icatx.versioning.CreateProjectSmallFiles";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
