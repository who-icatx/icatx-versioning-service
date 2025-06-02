package edu.stanford.protege.versioning.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectBackupSchedulerTest {

    @Mock
    private ProjectBackupService backupService;

    @InjectMocks
    private ProjectBackupScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "projectIds", "project1,project2,project3");
    }

    @Test
    void shouldBackupAllConfiguredProjects() {
        // when
        scheduler.scheduledBackup();

        // then
        verify(backupService, times(1)).createBackup("project1");
        verify(backupService, times(1)).createBackup("project2");
        verify(backupService, times(1)).createBackup("project3");
        verifyNoMoreInteractions(backupService);
    }

    @Test
    void shouldHandleEmptyProjectList() {
        // given
        ReflectionTestUtils.setField(scheduler, "projectIds", "");

        // when
        scheduler.scheduledBackup();

        // then
        verifyNoInteractions(backupService);
    }

    @Test
    void shouldHandleSingleProject() {
        // given
        ReflectionTestUtils.setField(scheduler, "projectIds", "project1");

        // when
        scheduler.scheduledBackup();

        // then
        verify(backupService, times(1)).createBackup("project1");
        verifyNoMoreInteractions(backupService);
    }

    @Test
    void shouldHandleBackupFailure() {
        // given
        doThrow(new RuntimeException("Backup failed"))
            .when(backupService).createBackup("project1");

        // when
        scheduler.scheduledBackup();

        // then
        verify(backupService, times(1)).createBackup("project1");
        verify(backupService, times(1)).createBackup("project2");
        verify(backupService, times(1)).createBackup("project3");
        verifyNoMoreInteractions(backupService);
    }

    @Test
    void shouldTrimProjectIds() {
        // given
        ReflectionTestUtils.setField(scheduler, "projectIds", " project1 , project2 ");

        // when
        scheduler.scheduledBackup();

        // then
        verify(backupService, times(1)).createBackup("project1");
        verify(backupService, times(1)).createBackup("project2");
        verifyNoMoreInteractions(backupService);
    }
} 