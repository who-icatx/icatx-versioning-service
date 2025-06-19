package edu.stanford.protege.versioning.services;

import edu.stanford.protege.versioning.KeycloakExecutionContextHelper;
import edu.stanford.protege.versioning.SecurityContextHelper;
import edu.stanford.protege.versioning.entity.ReproducibleProject;
import edu.stanford.protege.versioning.repository.ReproducibleProjectsRepository;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectBackupScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectBackupScheduler.class);

    @Autowired
    private ProjectBackupService backupService;

    @Value("${webprotege.backup.projectIds}")
    private String projectIds;

    @Autowired
    private ReproducibleProjectsRepository reproducibleProjectsRepository;

    @Autowired
    private KeycloakService keycloakService;

    @Scheduled(cron = "0 * * * * ?")
    //    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduledBackup() {
        LOGGER.info("Starting scheduled backup for all configured projects");

        List<String> projects = reproducibleProjectsRepository.findAll().stream()
                .map(ReproducibleProject::getProjectId)
                .toList();

        if (projects.isEmpty()) {
            LOGGER.info("No projects configured for backup");
            return;
        }
        
        // Get Keycloak access token for service authentication
        String accessToken;
        try {
            accessToken = keycloakService.getAccessToken();
            LOGGER.info("Successfully obtained Keycloak access token for scheduled backup");
        } catch (Exception e) {
            LOGGER.error("Failed to obtain Keycloak access token for scheduled backup", e);
            return;
        }
        
        for (String projectId : projects) {
            try {
                LOGGER.info("Starting backup for project: {}", projectId);
                // Use Keycloak service execution context instead of SecurityContextHelper
                backupService.createBackup(projectId, KeycloakExecutionContextHelper.createServiceExecutionContext(accessToken));
                LOGGER.info("Successfully completed backup for project: {}", projectId);
            } catch (Exception e) {
                LOGGER.error("Failed to backup project: " + projectId, e);
            }
        }
    }
} 