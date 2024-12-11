package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.dispatch.Result;

import static edu.stanford.protege.versioning.handlers.PrepareBackupFilesForUseRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record PrepareBackupFilesForUseResponse(
        @JsonProperty("binaryFileLocation") BlobLocation binaryFileLocation
) implements Result {

    public static PrepareBackupFilesForUseResponse create(BlobLocation binaryFileLocation) {
        return new PrepareBackupFilesForUseResponse(binaryFileLocation);
    }
}
