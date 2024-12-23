package edu.stanford.protege.versioning.owl.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import javax.annotation.Nonnull;

@JsonTypeName(CreateBackupOwlFileRequest.CHANNEL)

public record CreateBackupOwlFileRequest(
        @JsonProperty("projectId") ProjectId projectId) implements ProjectRequest<CreateBackupOwlFileResponse> {
    public final static String CHANNEL = "webprotege.projects.CreateBackupOwlFile";


    public static CreateBackupOwlFileRequest create(ProjectId projectId) {
        return new CreateBackupOwlFileRequest(projectId);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Nonnull
    @Override
    public ProjectId projectId() {
        return projectId;
    }
}
