package edu.stanford.protege.versioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;


@JsonTypeName(GetProjectEntityInfoRequest.CHANNEL)
public record GetProjectEntityInfoRequest(@JsonProperty("projectId") ProjectId projectId,
                                          @JsonProperty("entityIri") IRI entityIri) implements Request<GetProjectEntityInfoResponse> {

    public final static String CHANNEL = "webprotege.icatxgateway.GetEntityInfo";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
