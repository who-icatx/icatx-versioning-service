package edu.stanford.protege.versioning.services;

import edu.stanford.protege.versioning.KeycloakExecutionContextHelper;
import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectBackupSchedulerTest {

    @Mock
    private ProjectBackupService backupService;

    @Mock
    private ReproducibleProjectsRepository reproducibleProjectsRepository;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private ProjectBackupScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "projectIds", "project1,project2,project3");
    }

    @Test
    void shouldBackupAllConfiguredProjectsWithKeycloakToken() {
        // Given
        List<ReproducibleProject> projects = Arrays.asList(
            createReproducibleProject("project1"),
            createReproducibleProject("project2"),
            createReproducibleProject("project3")
        );
        
        when(reproducibleProjectsRepository.findAll()).thenReturn(projects);
        when(keycloakService.getAccessToken()).thenReturn("test-keycloak-token");

        // When
        scheduler.scheduledBackup();

        // Then
        verify(keycloakService, times(1)).getAccessToken();
        verify(backupService, times(1)).createBackup(eq("project1"), any());
        verify(backupService, times(1)).createBackup(eq("project2"), any());
        verify(backupService, times(1)).createBackup(eq("project3"), any());
        verifyNoMoreInteractions(backupService);
    }

    @Test
    void shouldHandleEmptyProjectList() {
        // Given
        when(reproducibleProjectsRepository.findAll()).thenReturn(List.of());

        // When
        scheduler.scheduledBackup();

        // Then
        verifyNoInteractions(keycloakService, backupService);
    }

    @Test
    void shouldHandleKeycloakTokenFailure() {
        // Given
        List<ReproducibleProject> projects = Arrays.asList(
            createReproducibleProject("project1")
        );
        
        when(reproducibleProjectsRepository.findAll()).thenReturn(projects);
        when(keycloakService.getAccessToken()).thenThrow(new RuntimeException("Keycloak error"));

        // When
        scheduler.scheduledBackup();

        // Then
        verify(keycloakService, times(1)).getAccessToken();
        verifyNoInteractions(backupService);
    }

    @Test
    void shouldContinueBackupWhenOneProjectFails() {
        // Given
        List<ReproducibleProject> projects = Arrays.asList(
            createReproducibleProject("project1"),
            createReproducibleProject("project2")
        );
        
        when(reproducibleProjectsRepository.findAll()).thenReturn(projects);
        when(keycloakService.getAccessToken()).thenReturn("test-keycloak-token");
        when(backupService.createBackup(eq("project1"), any())).thenThrow(new RuntimeException("Backup failed"));
        when(backupService.createBackup(eq("project2"), any())).thenReturn(Arrays.asList());

        // When
        scheduler.scheduledBackup();

        // Then
        verify(keycloakService, times(1)).getAccessToken();
        verify(backupService, times(1)).createBackup(eq("project1"), any());
        verify(backupService, times(1)).createBackup(eq("project2"), any());
    }

    private ReproducibleProject createReproducibleProject(String projectId) {
        ReproducibleProject project = new ReproducibleProject();
        project.setProjectId(projectId);
        return project;
    }
} 