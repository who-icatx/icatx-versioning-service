package edu.stanford.protege.versioning.services.git;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
public class GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitService.class);

    private static final String COMMIT_BACKUP_SCRIPT = "/app/commitBackup.sh";

    private static final int MAX_LISTED_ENTITIES = 25;

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

    private String buildCommitMessage(String entitiesCsv) {
        if (entitiesCsv == null || entitiesCsv.isBlank()) {
            return "No entities changed.";
        }

        String[] entities = Arrays.stream(entitiesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        int count = entities.length;
        if (count == 0) {
            return "No entities changed.";
        }

        if (count <= MAX_LISTED_ENTITIES) {
            return "Changed " + String.join(", ", entities) + ".";
        } else {
            return count + " entities changed.";
        }
    }

    public void commitAndPushChanges(String repoPath, String archivePath, String commitMessage ) {
        try {
            String formattedMessage = buildCommitMessage(commitMessage);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    COMMIT_BACKUP_SCRIPT,
                    repoPath,
                    archivePath,
                    formattedMessage
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
