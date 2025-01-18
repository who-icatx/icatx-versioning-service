package edu.stanford.protege.versioning.owl;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Stopwatch;
import edu.stanford.protege.versioning.*;
import edu.stanford.protege.versioning.entity.*;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.history.*;
import edu.stanford.protege.versioning.owl.commands.*;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class OwlClassesService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OwlClassesService.class);

    private final CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand;

    private final CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo;
    private final CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor;
    private final CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor;

    private final FileService fileService;

    private final GitService gitService;
    private final ReproducibleProjectsRepository reproducibleProjectsRepository;

    @Value("${webprotege.versioning.jsonFileLocation}")
    private String jsonFileLocation;

    public OwlClassesService(CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand,
                             CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo,
                             CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor,
                             CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor,
                             FileService fileService,
                             GitService gitService, ReproducibleProjectsRepository reproducibleProjectsRepository) {
        this.getAllClassesCommand = getAllClassesCommand;
        this.getEntityInfo = getEntityInfo;
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.createBackupOwlFileExecutor = createBackupOwlFileExecutor;
        this.fileService = fileService;
        this.gitService = gitService;
        this.reproducibleProjectsRepository = reproducibleProjectsRepository;
    }


    public List<IRI> saveInitialOntologyInfo(ProjectId projectId) throws ExecutionException, InterruptedException {
        var reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId.id());

        if (reproducibleProject == null) {
            throw new ApplicationException("Project id not found " + projectId.id());
        }

        gitService.gitInitRepo(reproducibleProject.getAssociatedBranch(), projectId.id());

        List<IRI> initialIris = getAllClassesCommand.execute(new GetAllOwlClassesRequest(projectId), SecurityContextHelper.getExecutionContext()).get().owlClassList();
        var stopwatch = Stopwatch.createStarted();
        List<IRI> response = new ArrayList<>();
        int fileCount = 0;
        for (IRI iri : initialIris.subList(0, 15)) {
            try {
                if (!fileService.getEntityFile(iri, projectId).exists()) {
                    JsonNode dto = getEntityInfo.execute(new GetProjectEntityInfoRequest(projectId, iri), SecurityContextHelper.getExecutionContext()).get().entityDto();
                    fileService.writeEntities(iri, dto, projectId);
                    response.add(iri);
                    fileCount++;
                }

            } catch (Exception e) {
                LOGGER.error("Error fetching " + iri);
            }
        }
        stopwatch.stop();
        LOGGER.info("{} Written {} entities in {} ms",
                projectId,
                fileCount,
                stopwatch.elapsed()
                        .toMillis());
        gitService.commitAndPushChanges(jsonFileLocation + projectId.id(), "" , "Initial commit");
        return response;
    }


    public List<IRI> getAllChangedEntitiesSinceLastBackupDate(ProjectId projectId){
        ReproducibleProject reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId.id());
        if (reproducibleProject == null) {
            throw new ApplicationException("Project id not found " + projectId);
        }
        ChangedEntities changedEntities;
        try {
            changedEntities = changedEntitiesExecutor.execute(new GetChangedEntitiesRequest(projectId, reproducibleProject.getLastBackupTimestamp()), SecurityContextHelper.getExecutionContext())
                    .get()
                    .changedEntities();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Couldn't fetch changed entities for " + projectId.id());
            throw new RuntimeException("Couldn't fetch changed entities for " + projectId.id(), e);
        }

        changedEntities.createdEntities().addAll(changedEntities.updatedEntities());
        return changedEntities.createdEntities().stream().map(IRI::create).toList();
    }

    @Async
    public void saveEntitiesSinceLastBackupDate(ProjectId projectId,
                                                List<IRI> allChangeEntities,
                                                ReproducibleProject reproducibleProject,
                                                ExecutionContext executionContext) {
        try {
            Map<IRI, JsonNode> changedEntitiesInfo = new HashMap<>();
            for (IRI iri : allChangeEntities) {
                try {
                    JsonNode dto = getEntityInfo.execute(new GetProjectEntityInfoRequest(projectId, iri), executionContext).get().entityDto();
                    changedEntitiesInfo.put(iri, dto);
                } catch (Throwable e) {
                    LOGGER.info("Error fetching IRI " + iri, e);
                }
            }

            for (IRI iri : changedEntitiesInfo.keySet()) {
                try {
                    fileService.writeEntities(iri, changedEntitiesInfo.get(iri), projectId);
                } catch (Exception e) {
                    LOGGER.error("Error writing file for IRI " + iri, e);
                }
            }

            reproducibleProject.setLastBackupTimestamp(Instant.now().toEpochMilli());
            reproducibleProjectsRepository.save(reproducibleProject);
        } catch (Exception e) {
            LOGGER.error("Error fetching changed entities", e);
            throw new ApplicationException("Error fetching changed entities");
        }
    }

    @Async
    public CompletableFuture<String> makeBackupForOwlBinaryFile(ProjectId projectId) {
        try {
            return createBackupOwlFileExecutor.execute(CreateBackupOwlFileRequest.create(projectId), SecurityContextHelper.getExecutionContext())
                    .thenApply(CreateBackupOwlFileResponse::owlFileBackupLocation);
        } catch (Exception e) {
            LOGGER.error("Error creating backup for owl file for projectId:" + projectId, e);
            throw new ApplicationException("Error creating backup for owl file for projectId:" + projectId);
        }
    }
}
