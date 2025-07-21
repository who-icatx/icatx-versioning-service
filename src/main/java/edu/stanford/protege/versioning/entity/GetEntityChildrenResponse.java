package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

import static edu.stanford.protege.versioning.entity.GetEntityChildrenRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityChildrenResponse(@JsonProperty("childrenIris") List<IRI> childrenIris) implements Response {
}
