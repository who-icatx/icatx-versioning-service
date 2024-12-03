package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;



@JsonTypeName(GetProjectEntityInfoRequest.CHANNEL)
public record GetProjectEntityInfoResponse(OWLEntityDto entityDto) implements Response {
}
