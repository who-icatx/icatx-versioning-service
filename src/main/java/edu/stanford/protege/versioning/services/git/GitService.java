package edu.stanford.protege.versioning.services.git;

import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitService.class);

    private static final String COMMIT_BACKUP_SCRIPT = "/app/commitBackup.sh";

    public void commitAndPushChanges(String repoPath, String archivePath, String commitMessage ) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    COMMIT_BACKUP_SCRIPT,
                    repoPath,
                    archivePath,
                    commitMessage
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
    }
}
