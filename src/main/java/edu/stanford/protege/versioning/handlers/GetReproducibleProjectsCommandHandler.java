package edu.stanford.protege.versioning.handlers;

import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;

@WebProtegeHandler
public class GetReproducibleProjectsCommandHandler implements CommandHandler<GetReproducibleProjectsRequest, GetReproducibleProjectsResponse> {

    private final ReproducibleProjectsRepository reproducibleProjectsRepository;

    public GetReproducibleProjectsCommandHandler(ReproducibleProjectsRepository reproducibleProjectsRepository) {
        this.reproducibleProjectsRepository = reproducibleProjectsRepository;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return GetReproducibleProjectsRequest.CHANNEL;
    }

    @Override
    public Class<GetReproducibleProjectsRequest> getRequestClass() {
        return GetReproducibleProjectsRequest.class;
    }

    @Override
    public Mono<GetReproducibleProjectsResponse> handleRequest(GetReproducibleProjectsRequest request, ExecutionContext executionContext) {
        List<ReproducibleProject> reproducibleProjectList = reproducibleProjectsRepository.findAll();
        return Mono.just(new GetReproducibleProjectsResponse(reproducibleProjectList));
    }
}
