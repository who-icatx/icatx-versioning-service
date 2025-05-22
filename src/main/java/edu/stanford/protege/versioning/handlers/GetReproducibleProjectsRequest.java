package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(GetReproducibleProjectsRequest.CHANNEL)

public record GetReproducibleProjectsRequest() implements Request<GetReproducibleProjectsResponse> {
    public static final String CHANNEL = "icatx.versioning.GetReproducibleProjects";


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
