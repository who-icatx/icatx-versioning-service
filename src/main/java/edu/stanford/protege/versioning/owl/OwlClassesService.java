package edu.stanford.protege.versioning.owl;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Stopwatch;
import edu.stanford.protege.versioning.SecurityContextHelper;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoRequest;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoResponse;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesRequest;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class OwlClassesService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OwlClassesService.class);

    private final CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand;

    private final CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo;

    private final FileService fileService;

    public OwlClassesService(CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand, CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo, FileService fileService) {
        this.getAllClassesCommand = getAllClassesCommand;
        this.getEntityInfo = getEntityInfo;
        this.fileService = fileService;
    }


    public List<IRI> getAllClasses(ProjectId projectId) throws ExecutionException, InterruptedException {
        List<IRI> response = getAllClassesCommand.execute(new GetAllOwlClassesRequest(projectId), SecurityContextHelper.getExecutionContext()).get().owlClassList();
        var stopwatch = Stopwatch.createStarted();
        int fileCount = 0;
        for(IRI iri : response) {
            try {
                if(!fileService.getEntityFile(iri, projectId).exists()) {
                    JsonNode dto = getEntityInfo.execute(new GetProjectEntityInfoRequest(projectId, iri), SecurityContextHelper.getExecutionContext()).get().entityDto();
                    fileService.writeEntities(iri, dto, projectId);
                    LOGGER.error("Writing entity to file " + iri);
                    fileCount++;
                }

            }catch (Exception e){
                LOGGER.error("Error fetching " + iri);
            }
        }
        stopwatch.stop();
        LOGGER.error("{} Written {} entities in {} ms",
                projectId,
                fileCount,
                stopwatch.elapsed()
                        .toMillis());
        return response;
    }

}
