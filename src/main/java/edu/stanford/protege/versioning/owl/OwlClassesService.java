package edu.stanford.protege.versioning.owl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import edu.stanford.protege.versioning.*;
import edu.stanford.protege.versioning.entity.*;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.handlers.UpdateEntityChildrenRequest;
import edu.stanford.protege.versioning.history.*;
import edu.stanford.protege.versioning.owl.commands.*;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.versioning.repository.BlacklistedIriRepository;
import edu.stanford.protege.versioning.entity.BlacklistedIri;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OwlClassesService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OwlClassesService.class);

    private final CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand;

    private final CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo;
    private final CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor;
    private final CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor;
    private final CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;

    private final FileService fileService;

    private final GitService gitService;
    private final ReproducibleProjectsRepository reproducibleProjectsRepository;
    private final BlacklistedIriRepository blacklistedIriRepository;

    private final ObjectMapper objectMapper;

    @Value("${webprotege.versioning.jsonFileLocation}")
    private String jsonFileLocation;

    public OwlClassesService(CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand,
                             CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo,
                             CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor,
                             CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor,
                             CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor, FileService fileService,
                             GitService gitService, ReproducibleProjectsRepository reproducibleProjectsRepository, 
                             BlacklistedIriRepository blacklistedIriRepository, ObjectMapper objectMapper) {
        this.getAllClassesCommand = getAllClassesCommand;
        this.getEntityInfo = getEntityInfo;
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.createBackupOwlFileExecutor = createBackupOwlFileExecutor;
        this.entityChildrenExecutor = entityChildrenExecutor;
        this.fileService = fileService;
        this.gitService = gitService;
        this.reproducibleProjectsRepository = reproducibleProjectsRepository;
        this.blacklistedIriRepository = blacklistedIriRepository;
        this.objectMapper = objectMapper;
    }

    private Set<String> getBlacklistedIris() {
        return blacklistedIriRepository.findAll().stream()
                .map(BlacklistedIri::getIri)
                .collect(Collectors.toSet());
    }

    public List<IRI> saveInitialOntologyInfo(ProjectId projectId, ExecutionContext executionContext) throws ExecutionException, InterruptedException {
        var reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId.id());

        if (reproducibleProject == null) {
            throw new ApplicationException("Project id not found " + projectId.id());
        }

        gitService.gitInitRepo(reproducibleProject.getAssociatedBranch(), projectId.id());

        List<IRI> initialIris = getAllClassesCommand.execute(new GetAllOwlClassesRequest(projectId), executionContext).get().owlClassList();
        Set<String> blacklistedIris = getBlacklistedIris();
        var stopwatch = Stopwatch.createStarted();
        List<IRI> response = new ArrayList<>();
        int fileCount = 0;
        for (IRI iri : initialIris) {
            try {
                // Skip if IRI is blacklisted
                if (blacklistedIris.contains(iri.toString())) {
                    LOGGER.info("Skipping blacklisted IRI: {}", iri);
                    continue;
                }
                
                if (!fileService.getEntityFile(iri, projectId).exists()) {
                    CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
                    JsonNode dto = getEntityInfo.execute(new GetProjectEntityInfoRequest(projectId, iri), executionContext).get().entityDto();
                    fileService.writeEntities(iri, dto, projectId);
                    response.add(iri);
                    fileCount++;
                }

            } catch (Exception e) {
                LOGGER.error("Error fetching " + iri,e);
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


    public List<IRI> getAllChangedEntitiesSinceLastBackupDate(ProjectId projectId, ExecutionContext executionContext){
        ReproducibleProject reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId.id());
        if (reproducibleProject == null) {
            throw new ApplicationException("Project id not found " + projectId);
        }
        ChangedEntities changedEntities;
        try {
            changedEntities = changedEntitiesExecutor.execute(new GetChangedEntitiesRequest(projectId, reproducibleProject.getLastBackupTimestamp()), executionContext)
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
            Set<String> blacklistedIris = getBlacklistedIris();
            Map<IRI, JsonNode> changedEntitiesInfo = new HashMap<>();
            for (IRI iri : allChangeEntities) {
                try {
                    // Skip if IRI is blacklisted
                    if (blacklistedIris.contains(iri.toString())) {
                        LOGGER.info("Skipping blacklisted IRI: {}", iri);
                        continue;
                    }
                    
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

    public void initialEntitiesChildrenSave(ProjectId projectId, ExecutionContext executionContext) throws ExecutionException, InterruptedException, TimeoutException {
        List<IRI> initialIris = getAllClassesCommand.execute(new GetAllOwlClassesRequest(projectId), executionContext).get(35, TimeUnit.SECONDS).owlClassList();
        Set<String> blacklistedIris = getBlacklistedIris();
        for (IRI iri : initialIris) {
            try {
                // Skip if IRI is blacklisted
                if (blacklistedIris.contains(iri.toString())) {
                    LOGGER.info("Skipping blacklisted IRI: {}", iri);
                    continue;
                }
                
                CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
                GetEntityChildrenResponse dto = entityChildrenExecutor.execute(new GetEntityChildrenRequest(iri, projectId), executionContext)
                        .get(5, TimeUnit.SECONDS);
                if(dto.childrenIris() != null && !dto.childrenIris().isEmpty()) {
                    fileService.writeEntityChildrenFile(new EntityChildren(projectId.id(), iri.toString(), dto.childrenIris().stream().map(IRI::toString).toList()));
                }
            } catch (Exception e) {
                LOGGER.error("Error fetching " + iri);
            }
        }
        gitService.commitAndPushChanges(jsonFileLocation + projectId.id(), "" , "Initial children files commit");
    }


    public void saveOrUpdateEntityChildren(UpdateEntityChildrenRequest request) {
        try {
            CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
            if(request.childrenIris() != null && !request.childrenIris().isEmpty()) {
                fileService.writeEntityChildrenFile(new EntityChildren(request.projectId().id(), request.entityIri().toString(), request.childrenIris()));
            } else {
                fileService.removeFileIfExists(request.projectId(), request.entityIri());
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching " + request.entityIri());
        }
    }

    @Async
    public CompletableFuture<String> makeBackupForOwlBinaryFile(ProjectId projectId, ExecutionContext executionContext) {
        try {
            return createBackupOwlFileExecutor.execute(CreateBackupOwlFileRequest.create(projectId), executionContext)
                    .thenApply(CreateBackupOwlFileResponse::owlFileBackupLocation);
        } catch (Exception e) {
            LOGGER.error("Error creating backup for owl file for projectId:" + projectId, e);
            throw new ApplicationException("Error creating backup for owl file for projectId:" + projectId);
        }
    }
}
