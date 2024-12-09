package edu.stanford.protege.versioning.handlers;

import edu.stanford.protege.versioning.executors.ontology.OntologyService;
import edu.stanford.protege.versioning.services.backupProcessor.BackupFilesProcessor;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.project.NewProjectSettings;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;

import static edu.stanford.protege.versioning.access.BuiltInAction.*;


@WebProtegeHandler
public class CreateNewProjectFromProjectBackupCommandHandler implements AuthorizedCommandHandler<CreateNewProjectFromProjectBackupAction, CreateNewProjectFromProjectBackupResult> {

    private final OntologyService ontologyService;
    private final BackupFilesProcessor backupFilesProcessor;


    public CreateNewProjectFromProjectBackupCommandHandler(OntologyService ontologyService,
                                                           BackupFilesProcessor backupFilesProcessor) {
        this.ontologyService = ontologyService;
        this.backupFilesProcessor = backupFilesProcessor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return CreateNewProjectFromProjectBackupAction.CHANNEL;
    }

    @Override
    public Class<CreateNewProjectFromProjectBackupAction> getRequestClass() {
        return CreateNewProjectFromProjectBackupAction.class;
    }

    @Override
    public Mono<CreateNewProjectFromProjectBackupResult> handleRequest(CreateNewProjectFromProjectBackupAction request,
                                                                       ExecutionContext executionContext) {
        var projectSettings = request.newProjectSettings();
        var projectDetails = this.ontologyService.createNewProject(
                request.newProjectId(),
                NewProjectSettings.get(projectSettings.getProjectOwner(),
                        projectSettings.getDisplayName(),
                        projectSettings.getLangTag(),
                        projectSettings.getProjectDescription())
        );

        if (projectSettings.hasSourceDocument()) {
            backupFilesProcessor.processBackupFiles(request.newProjectId(), projectSettings.getSourceDocumentId().get());
        }

        return Mono.just(CreateNewProjectFromProjectBackupResult.create(projectDetails));
    }

    @Nonnull
    @Override
    public Resource getTargetResource(CreateNewProjectFromProjectBackupAction request) {
        return ApplicationResource.get();
    }

    @Nonnull
    @Override
    public Collection<ActionId> getRequiredCapabilities() {
        return List.of(CREATE_EMPTY_PROJECT.getActionId(), UPLOAD_PROJECT.getActionId());
    }


}
