package edu.stanford.protege.versioning.handlers;

import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
import edu.stanford.protege.webprotege.ipc.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@WebProtegeHandler
public class PrepareBackupFilesForUseCommandHandler implements CommandHandler<PrepareBackupFilesForUseRequest, PrepareBackupFilesForUseResponse> {

    private final BackupFilesProcessor backupFilesProcessor;


    public PrepareBackupFilesForUseCommandHandler(BackupFilesProcessor backupFilesProcessor) {
        this.backupFilesProcessor = backupFilesProcessor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return PrepareBackupFilesForUseRequest.CHANNEL;
    }

    @Override
    public Class<PrepareBackupFilesForUseRequest> getRequestClass() {
        return PrepareBackupFilesForUseRequest.class;
    }

    @Override
    public Mono<PrepareBackupFilesForUseResponse> handleRequest(PrepareBackupFilesForUseRequest request,
                                                                ExecutionContext executionContext) {

        var response = backupFilesProcessor.prepareOwlBinaryAndImportCollections(request.projectId(), request.fileSubmissionId());

        return Mono.just(response);
    }


}
