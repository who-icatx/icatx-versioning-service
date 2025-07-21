package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.versioning.handlers.UpdateEntityChildrenRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public class UpdateEntityChildrenResponse implements Response {
}
