package edu.stanford.protege.versioning;

import edu.stanford.protege.versioning.entity.EntityChildren;
import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.versioning.handlers.UpdateEntityChildrenRequest;
import edu.stanford.protege.versioning.owl.OwlClassesService;
import edu.stanford.protege.versioning.entity.GetEntityChildrenRequest;
import edu.stanford.protege.versioning.entity.GetEntityChildrenResponse;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoRequest;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoResponse;
import edu.stanford.protege.versioning.history.GetChangedEntitiesRequest;
import edu.stanford.protege.versioning.history.GetChangedEntitiesResponse;
import edu.stanford.protege.versioning.owl.commands.CreateBackupOwlFileRequest;
import edu.stanford.protege.versioning.owl.commands.CreateBackupOwlFileResponse;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesRequest;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesResponse;
import edu.stanford.protege.versioning.services.git.GitService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwlClassesServiceTest {
    @Mock
    private CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> getAllClassesCommand;
    @Mock
    private CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityInfo;
    @Mock
    private CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor;
    @Mock
    private CommandExecutor<CreateBackupOwlFileRequest, CreateBackupOwlFileResponse> createBackupOwlFileExecutor;
    @Mock
    private CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;
    @Mock
    private FileService fileService;
    @Mock
    private GitService gitService;
    @Mock
    private CommandExecutor<?,?> dummyExecutor1, dummyExecutor2;
    @Mock
    private edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository reproducibleProjectsRepository;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private OwlClassesService service;

    @BeforeEach
    void setup() {
        // Use mocks for all required dependencies
        service = new OwlClassesService(
                getAllClassesCommand,
                getEntityInfo,
                changedEntitiesExecutor,
                createBackupOwlFileExecutor,
                entityChildrenExecutor,
                fileService,
                gitService,
                reproducibleProjectsRepository,
                objectMapper
        );
        // Set jsonFileLocation field if needed
        org.springframework.test.util.ReflectionTestUtils.setField(service, "jsonFileLocation", "/tmp/");
    }

    @Test
    void testInitialEntitiesChildrenSave_writesChildrenFilesAndCommits() throws Exception {
        ProjectId projectId = ProjectId.valueOf("123e4567-e89b-12d3-a456-426614174000");
        ExecutionContext ctx = mock(ExecutionContext.class);
        IRI iri1 = IRI.create("http://example.org/iri1");
        IRI iri2 = IRI.create("http://example.org/iri2");
        List<IRI> iris = List.of(iri1, iri2);
        List<IRI> children1 = List.of(IRI.create("http://example.org/child1"));
        List<IRI> children2 = List.of();

        // Mock getAllClassesCommand
        GetAllOwlClassesResponse allClassesResp = mock(GetAllOwlClassesResponse.class);
        when(getAllClassesCommand.execute(any(), any())).thenReturn(CompletableFuture.completedFuture(allClassesResp));
        when(allClassesResp.owlClassList()).thenReturn(iris);

        // Mock entityChildrenExecutor
        GetEntityChildrenResponse resp1 = mock(GetEntityChildrenResponse.class);
        when(resp1.childrenIris()).thenReturn(children1);
        GetEntityChildrenResponse resp2 = mock(GetEntityChildrenResponse.class);
        when(resp2.childrenIris()).thenReturn(children2);
        when(entityChildrenExecutor.execute(eq(new GetEntityChildrenRequest(iri1, projectId)), any()))
                .thenReturn(CompletableFuture.completedFuture(resp1));
        when(entityChildrenExecutor.execute(eq(new GetEntityChildrenRequest(iri2, projectId)), any()))
                .thenReturn(CompletableFuture.completedFuture(resp2));

        // Run
        service.initialEntitiesChildrenSave(projectId, ctx);

        // Only iri1 has children, so only one write
        verify(fileService, times(1)).writeEntityChildrenFile(argThat(ec ->
                ec.projectId().equals(projectId.id()) &&
                ec.entityUri().equals(iri1.toString()) &&
                ec.children().equals(children1.stream().map(IRI::toString).toList())
        ));
        verify(fileService, never()).writeEntityChildrenFile(argThat(ec -> ec.entityUri().equals(iri2.toString())));
        verify(gitService).commitAndPushChanges(
                eq("/tmp/123e4567-e89b-12d3-a456-426614174000"), anyString(), eq("Initial children files commit"));
    }

    @Test
    void testSaveOrUpdateEntityChildren_writesFile_whenChildrenPresent() {
        ProjectId projectId = ProjectId.valueOf("123e4567-e89b-12d3-a456-426614174000");
        IRI entityIri = IRI.create("http://example.org/iri");
        List<String> children = List.of("child1", "child2");
        UpdateEntityChildrenRequest req = new UpdateEntityChildrenRequest(projectId, entityIri, children);

        service.saveOrUpdateEntityChildren(req);

        verify(fileService).writeEntityChildrenFile(argThat(ec ->
                ec.projectId().equals(projectId.id()) &&
                ec.entityUri().equals(entityIri.toString()) &&
                ec.children().equals(children)
        ));
        verify(fileService, never()).removeFileIfExists(any(), any());
    }

    @Test
    void testSaveOrUpdateEntityChildren_removesFile_whenChildrenEmpty() {
        ProjectId projectId = ProjectId.valueOf("123e4567-e89b-12d3-a456-426614174000");
        IRI entityIri = IRI.create("http://example.org/iri");
        UpdateEntityChildrenRequest req = new UpdateEntityChildrenRequest(projectId, entityIri, List.of());

        service.saveOrUpdateEntityChildren(req);

        verify(fileService, never()).writeEntityChildrenFile(any());
        verify(fileService).removeFileIfExists(projectId, entityIri);
    }

    @Test
    void testSaveOrUpdateEntityChildren_removesFile_whenChildrenNull() {
        ProjectId projectId = ProjectId.valueOf("123e4567-e89b-12d3-a456-426614174000");
        IRI entityIri = IRI.create("http://example.org/iri");
        UpdateEntityChildrenRequest req = new UpdateEntityChildrenRequest(projectId, entityIri, null);

        service.saveOrUpdateEntityChildren(req);

        verify(fileService, never()).writeEntityChildrenFile(any());
        verify(fileService).removeFileIfExists(projectId, entityIri);
    }
}
