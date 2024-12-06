package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.protege.webprotege.common.Response;



@JsonTypeName(GetProjectEntityInfoRequest.CHANNEL)
public record GetProjectEntityInfoResponse(JsonNode entityDto) implements Response {
}
