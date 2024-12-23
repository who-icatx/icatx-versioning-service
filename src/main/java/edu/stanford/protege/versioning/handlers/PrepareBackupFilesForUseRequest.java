package edu.stanford.protege.versioning.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.versioning.dtos.DocumentId;
import edu.stanford.protege.webprotege.common.*;

@JsonTypeName(PrepareBackupFilesForUseRequest.CHANNEL)
public record PrepareBackupFilesForUseRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("fileSubmissionId") DocumentId fileSubmissionId
) implements Request<PrepareBackupFilesForUseResponse> {

    public static final String CHANNEL = "icatx.versioning.PrepareBinaryFileBackupForUse";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
