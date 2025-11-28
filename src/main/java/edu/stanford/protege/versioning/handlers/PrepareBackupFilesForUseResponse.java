package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.versioning.handlers.PrepareBackupFilesForUseRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record PrepareBackupFilesForUseResponse(
        @JsonProperty("binaryFileLocation") BlobLocation binaryFileLocation,
        @JsonProperty("projectDetailsJson") String projectDetailsJson
) implements Response {

    public static PrepareBackupFilesForUseResponse create(BlobLocation binaryFileLocation) {
        return new PrepareBackupFilesForUseResponse(binaryFileLocation, null);
    }

    public static PrepareBackupFilesForUseResponse create(BlobLocation binaryFileLocation, String projectDetailsJson) {
        return new PrepareBackupFilesForUseResponse(binaryFileLocation, projectDetailsJson);
    }
}
