package edu.stanford.protege.versioning.services;

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

    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduledBackup() {
        LOGGER.info("Starting scheduled backup for all configured projects");
        List<String> projects = Arrays.stream(projectIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
        
        if (projects.isEmpty()) {
            LOGGER.info("No projects configured for backup");
            return;
        }
        
        for (String projectId : projects) {
            try {
                LOGGER.info("Starting backup for project: {}", projectId);
                backupService.createBackup(projectId);
                LOGGER.info("Successfully completed backup for project: {}", projectId);
            } catch (Exception e) {
                LOGGER.error("Failed to backup project: " + projectId, e);
            }
        }
    }
} 