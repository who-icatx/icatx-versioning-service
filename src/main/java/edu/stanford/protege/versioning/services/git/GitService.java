package edu.stanford.protege.versioning.services.git;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitService.class);

    private static final String COMMIT_BACKUP_SCRIPT = "/app/commitBackup.sh";

    private static final int  MAX_SIZE_COMMIT_MESSAGE =  100;

    private static final String CHECKOUT_SCRIPT = "/app/gitCheckout.sh";

    private static final String GIT_INIT = "/app/gitInit.sh";

    @Value("${webprotege.versioning.sshUrl}")
    private String repoSsh;


    @Value("${webprotege.versioning.jsonFileLocation}")
    private String jsonFileLocation;

    public int gitCheckout(String branch, String repoPath) {
        try {
            logger.info("Trying to checkout the git on branch " + branch + " and repo path " + repoPath);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    CHECKOUT_SCRIPT,
                    repoPath,
                    repoSsh,
                    branch
            );

            // Redirect error stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the script output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SCRIPT OUTPUT] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Script exited with non-zero code: " + exitCode);
            }
            System.out.println("Script executed successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to run script", e);
        }

        return 0;
    }

    public void gitInitRepo(String branch, String projectId) {
        try {
            logger.info("Trying to git init " + repoSsh + " and repo path " + jsonFileLocation);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    GIT_INIT,
                    repoSsh,
                    jsonFileLocation + projectId,
                    branch
            );

            // Redirect error stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the script output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SCRIPT OUTPUT] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Script exited with non-zero code: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run script", e);
        }
    }

    private String truncateCommitMessage(String commitMessage) {
        if (commitMessage == null || commitMessage.length() <= MAX_SIZE_COMMIT_MESSAGE) {
            return commitMessage;
        }

        // Take first MAX_SIZE_COMMIT_MESSAGE characters
        String truncated = commitMessage.substring(0, MAX_SIZE_COMMIT_MESSAGE);
        
        // Find last comma in the truncated part
        int lastCommaIndex = truncated.lastIndexOf(',');
        
        if (lastCommaIndex == -1) {
            // No comma found, truncate at MAX_SIZE_COMMIT_MESSAGE
            truncated = commitMessage.substring(0, MAX_SIZE_COMMIT_MESSAGE);
            String remaining = commitMessage.substring(MAX_SIZE_COMMIT_MESSAGE);
            // Count remaining entities (commas + 1 for last entity if not ending with comma)
            int remainingEntities = remaining.split(",", -1).length;
            if (remainingEntities > 0) {
                truncated += " and " + remainingEntities + " entities more";
            }
        } else {
            // Truncate at last comma position (including the comma)
            truncated = commitMessage.substring(0, lastCommaIndex + 1);
            String remaining = commitMessage.substring(lastCommaIndex + 1);
            // Count remaining entities
            int remainingEntities = remaining.split(",", -1).length;
            if (remainingEntities > 0) {
                truncated += " and " + remainingEntities + " entities more";
            }
        }
        
        return truncated;
    }

    public void commitAndPushChanges(String repoPath, String archivePath, String commitMessage ) {
        try {
            String truncatedMessage = truncateCommitMessage(commitMessage);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    COMMIT_BACKUP_SCRIPT,
                    repoPath,
                    archivePath,
                    truncatedMessage
            );

            // Redirect error stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the script output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SCRIPT OUTPUT] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Script exited with non-zero code: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run script", e);
        }
    }
}
