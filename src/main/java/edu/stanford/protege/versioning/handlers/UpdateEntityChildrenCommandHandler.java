package edu.stanford.protege.versioning.handlers;


import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@WebProtegeHandler
public class UpdateEntityChildrenCommandHandler  implements CommandHandler<UpdateEntityChildrenRequest, UpdateEntityChildrenResponse>  {


    private final OwlClassesService owlClassesService;

    public UpdateEntityChildrenCommandHandler(OwlClassesService owlClassesService) {
        this.owlClassesService = owlClassesService;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return UpdateEntityChildrenRequest.CHANNEL;
    }

    @Override
    public Class<UpdateEntityChildrenRequest> getRequestClass() {
        return UpdateEntityChildrenRequest.class;
    }

    @Override
    public Mono<UpdateEntityChildrenResponse> handleRequest(UpdateEntityChildrenRequest request, ExecutionContext executionContext) {
        owlClassesService.saveOrUpdateEntityChildren(request.projectId(), request.entityIri(), executionContext);
        return Mono.just(new UpdateEntityChildrenResponse());
    }
}
