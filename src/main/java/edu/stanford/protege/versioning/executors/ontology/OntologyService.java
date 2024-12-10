package edu.stanford.protege.versioning.executors.ontology;


import edu.stanford.protege.versioning.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.project.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class OntologyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);
    private final CommandExecutor<CreateNewProjectAction, CreateNewProjectResult> createNewProjectExecutor;


    public OntologyService(CommandExecutor<CreateNewProjectAction, CreateNewProjectResult> createNewProjectExecutor) {
        this.createNewProjectExecutor = createNewProjectExecutor;
    }


    public ProjectDetails createNewProject(ProjectId newProjectId, NewProjectSettings newProjectSettings, ExecutionContext executionContext) {

        try {
            return createNewProjectExecutor.execute(new CreateNewProjectAction(newProjectId, newProjectSettings), executionContext)
                    .thenApply(CreateNewProjectResult::projectDetails)
                    .get();
        } catch (InterruptedException e) {
            String interException = "Interrupted exception while trying to create new project with projectId:"+newProjectId.id();
            LOGGER.error(interException,e);
            throw new ApplicationException(interException);
        } catch (ExecutionException e) {
            String execException = "Execution exception while trying to create new project with projectId:"+newProjectId.id();
            LOGGER.error(execException,e);
            throw new ApplicationException(execException);
        }

    }
}
