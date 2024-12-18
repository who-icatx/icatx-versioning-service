package edu.stanford.protege.versioning.config;

import edu.stanford.protege.versioning.services.git.GitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitConfig {

    @Value("${git.remote.url}")
    private String remoteUrl;

    @Value("${git.username}")
    private String username;

    @Value("${git.password}")
    private String password;

    @Bean
    public GitService gitService() {
        return new GitService(remoteUrl, username, password);
    }
}
