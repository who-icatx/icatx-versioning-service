package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

@JsonTypeName(GetEntityChildrenRequest.CHANNEL)
public record GetEntityChildrenRequest(
        @JsonProperty("classIri") IRI classIri,
        @JsonProperty("projectId") ProjectId projectId
) implements Request<GetEntityChildrenResponse> {
    public static final String CHANNEL = "webprotege.entities.GetEntityChildren";


    public static GetEntityChildrenRequest create(IRI classIri,
                                                  ProjectId projectId) {
        return new GetEntityChildrenRequest(classIri, projectId);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
