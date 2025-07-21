package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.versioning.handlers.UpdateEntityChildrenRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record UpdateEntityChildrenRequest(  @JsonProperty("projectId") ProjectId projectId,
                                            @JsonProperty("entityIri") IRI entityIri) implements Request<UpdateEntityChildrenResponse> {

    public final static String CHANNEL =  "icatx.versioning.UpdateEntityChildren";


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
