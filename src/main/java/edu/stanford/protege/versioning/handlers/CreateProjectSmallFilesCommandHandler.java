package edu.stanford.protege.versioning.handlers;

import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.webprotege.ipc.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@WebProtegeHandler
public class CreateProjectSmallFilesCommandHandler implements CommandHandler<CreateProjectSmallFilesRequest, CreateProjectSmallFilesResponse> {

    private final FileService fileService;


    public CreateProjectSmallFilesCommandHandler(FileService fileService) {
        this.fileService = fileService;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return CreateProjectSmallFilesRequest.CHANNEL;
    }

    @Override
    public Class<CreateProjectSmallFilesRequest> getRequestClass() {
        return CreateProjectSmallFilesRequest.class;
    }

    @Override
    public Mono<CreateProjectSmallFilesResponse> handleRequest(CreateProjectSmallFilesRequest request,
                                                               ExecutionContext executionContext) {

        fileService.createSmallFilesDirectory(request.projectId());
        return Mono.just(new CreateProjectSmallFilesResponse());
    }


}
