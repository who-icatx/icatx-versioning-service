package edu.stanford.protege.versioning.owl.commands;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

public record GetAllOwlClassesRequest(ProjectId projectId) implements Request<GetAllOwlClassesResponse> {
    public final static String CHANNEL = "webprotege.entities.GetAllOwlClasses";


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
