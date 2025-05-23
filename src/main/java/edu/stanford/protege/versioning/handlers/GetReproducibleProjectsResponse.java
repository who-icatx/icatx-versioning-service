package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.versioning.handlers.GetReproducibleProjectsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetReproducibleProjectsResponse(List<ReproducibleProject> reproducibleProjectList) implements Response {
}
