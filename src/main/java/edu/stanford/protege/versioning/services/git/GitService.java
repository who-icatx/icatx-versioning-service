package edu.stanford.protege.versioning.services.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.*;

import java.io.File;

public class GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitService.class);

    private final String remoteRepositoryUrl;
    private final String gitUsername;
    private final String gitPassword;

    public GitService(String remoteRepositoryUrl,
                      String gitUsername,
                      String gitPassword) {
        this.remoteRepositoryUrl = remoteRepositoryUrl;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    public void commitAndPushChanges(String commitMessage, String repositoryPath) {
        try (Git git = Git.open(new File(repositoryPath))) {

            git.add().addFilepattern(".").call();
            logger.info("All changes added to Git staging area.");

            git.commit().setMessage(commitMessage).call();
            logger.info("Changes committed with message: {}", commitMessage);

            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
                    .setRemote(remoteRepositoryUrl)
                    .call();
            logger.info("Changes pushed to remote repository: {}", remoteRepositoryUrl);

        } catch (GitAPIException | java.io.IOException e) {
            logger.error("Git operation failed", e);
            throw new RuntimeException("Failed to perform Git operations", e);
        }
    }
}
