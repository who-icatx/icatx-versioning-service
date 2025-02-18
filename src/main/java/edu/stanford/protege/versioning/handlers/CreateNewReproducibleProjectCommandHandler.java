package edu.stanford.protege.versioning.handlers;

import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.webprotege.ipc.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.Instant;


@WebProtegeHandler
public class CreateNewReproducibleProjectCommandHandler implements CommandHandler<CreateNewReproducibleProjectRequest, CreateNewReproducibleProjectResponse> {

    private final FileService fileService;

    private final ReproducibleProjectsRepository reproducibleProjectsRepository;


    public CreateNewReproducibleProjectCommandHandler(FileService fileService, ReproducibleProjectsRepository reproducibleProjectsRepository) {
        this.fileService = fileService;
        this.reproducibleProjectsRepository = reproducibleProjectsRepository;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return CreateNewReproducibleProjectRequest.CHANNEL;
    }

    @Override
    public Class<CreateNewReproducibleProjectRequest> getRequestClass() {
        return CreateNewReproducibleProjectRequest.class;
    }

    @Override
    public Mono<CreateNewReproducibleProjectResponse> handleRequest(CreateNewReproducibleProjectRequest request,
                                                                    ExecutionContext executionContext) {

        fileService.createSmallFilesDirectory(request.projectId());
        var reproducibleProject = new ReproducibleProject();
        reproducibleProject.setProjectId(request.projectId().id());
        reproducibleProject.setLastBackupTimestamp(Instant.now().toEpochMilli());
        reproducibleProject.setAssociatedBranch(request.branch());


        reproducibleProjectsRepository.save(reproducibleProject);

        return Mono.just(new CreateNewReproducibleProjectResponse());
    }


}
