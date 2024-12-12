package edu.stanford.protege.versioning.history;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.versioning.history.GetChangedEntitiesRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetChangedEntitiesResponse(@JsonProperty("changedEntities") ChangedEntities changedEntities) implements Response {
}
