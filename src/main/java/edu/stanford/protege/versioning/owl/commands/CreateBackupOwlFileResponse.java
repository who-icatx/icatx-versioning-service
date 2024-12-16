package edu.stanford.protege.versioning.owl.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.versioning.owl.commands.CreateBackupOwlFileRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateBackupOwlFileResponse(
        @JsonProperty("owlFileBackupLocation") String owlFileBackupLocation
) implements Response {
}
