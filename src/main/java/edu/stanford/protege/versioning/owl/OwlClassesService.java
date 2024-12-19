package edu.stanford.protege.versioning.owl;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Stopwatch;
import edu.stanford.protege.versioning.*;
import edu.stanford.protege.versioning.entity.*;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.history.*;
import edu.stanford.protege.versioning.owl.commands.*;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
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

    private final ReproducibleProjectsRepository reproducibleProjectsRepository;

    public OwlClassesService(CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand,
                             CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo,
                             CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor,
                             CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor,
                             FileService fileService,
                             ReproducibleProjectsRepository reproducibleProjectsRepository) {
        this.getAllClassesCommand = getAllClassesCommand;
        this.getEntityInfo = getEntityInfo;
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.createBackupOwlFileExecutor = createBackupOwlFileExecutor;
        this.fileService = fileService;
        this.reproducibleProjectsRepository = reproducibleProjectsRepository;
    }


    public List<IRI> saveInitialOntologyInfo(ProjectId projectId) throws ExecutionException, InterruptedException {
        List<IRI> initialIris = getAllClassesCommand.execute(new GetAllOwlClassesRequest(projectId), SecurityContextHelper.getExecutionContext()).get().owlClassList();
        var stopwatch = Stopwatch.createStarted();
        List<IRI> response = new ArrayList<>();
        int fileCount = 0;
        for (IRI iri : initialIris) {
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
        return response;
    }


    public List<IRI> saveEntitiesSinceLastBackupDate(ProjectId projectId) {
        List<IRI> response = new ArrayList<>();
        try {
            ReproducibleProject reproducibleProject = reproducibleProjectsRepository.findByProjectId(projectId.id());
            if (reproducibleProject == null) {
                throw new ApplicationException("Project id not found " + projectId);
            }
            ChangedEntities changedEntities = changedEntitiesExecutor.execute(new GetChangedEntitiesRequest(projectId, reproducibleProject.getLastBackupTimestamp()), SecurityContextHelper.getExecutionContext())
                    .get()
                    .changedEntities();

            changedEntities.createdEntities().addAll(changedEntities.updatedEntities());
            List<IRI> allChangeEntities = changedEntities.createdEntities().stream().map(IRI::create).toList();
            Map<IRI, JsonNode> changedEntitiesInfo = new HashMap<>();
            for (IRI iri : allChangeEntities) {
                try {
                    JsonNode dto = getEntityInfo.execute(new GetProjectEntityInfoRequest(projectId, iri), SecurityContextHelper.getExecutionContext()).get().entityDto();
                    changedEntitiesInfo.put(iri, dto);
                } catch (Exception e) {
                    LOGGER.error("Error fetching IRI " + iri, e);
//                    throw new ApplicationException("Error fetching IRI " + iri, e);
                }
            }

            for (IRI iri : changedEntitiesInfo.keySet()) {
                fileService.writeEntities(iri, changedEntitiesInfo.get(iri), projectId);
                response.add(iri);
            }

            reproducibleProject.setLastBackupTimestamp(Instant.now().toEpochMilli());
            reproducibleProjectsRepository.save(reproducibleProject);
        } catch (Exception e) {
            LOGGER.error("Error fetching changed entities", e);
            throw new ApplicationException("Error fetching changed entities");
        }
        return response;
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
