package edu.stanford.protege.versioning.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class KeycloakService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${webprotege.keycloak.token-url}")
    private String tokenUrl;

    @Value("${webprotege.keycloak.client-id}")
    private String clientId;

    @Value("${webprotege.keycloak.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;
    
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public String getAccessToken() {
        CachedToken currentToken = cachedToken.get();
        
        // Check if we have a valid cached token
        if (currentToken != null && !currentToken.isExpired()) {
            LOGGER.debug("Using cached access token");
            return currentToken.accessToken;
        }

        // Fetch new token
        LOGGER.info("Fetching new access token from Keycloak");
        TokenResponse tokenResponse = fetchTokenFromKeycloak();
        
        // Cache the new token
        CachedToken newToken = new CachedToken(
            tokenResponse.accessToken,
            Instant.now().plusSeconds(tokenResponse.expiresIn - 30) // Buffer of 30 seconds
        );
        cachedToken.set(newToken);
        
        LOGGER.info("Successfully obtained new access token from Keycloak");
        return tokenResponse.accessToken;
    }

    private TokenResponse fetchTokenFromKeycloak() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to obtain token from Keycloak. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching token from Keycloak", e);
            throw new RuntimeException("Failed to authenticate with Keycloak", e);
        }
    }

    // Token response DTO
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("expires_in")
        private int expiresIn;
        
        @JsonProperty("refresh_expires_in")
        private int refreshExpiresIn;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("not-before-policy")
        private int notBeforePolicy;
        
        @JsonProperty("scope")
        private String scope;

        // Getters
        public String getAccessToken() { return accessToken; }
        public int getExpiresIn() { return expiresIn; }
        public int getRefreshExpiresIn() { return refreshExpiresIn; }
        public String getTokenType() { return tokenType; }
        public int getNotBeforePolicy() { return notBeforePolicy; }
        public String getScope() { return scope; }

        // Setters
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
        public void setRefreshExpiresIn(int refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public void setNotBeforePolicy(int notBeforePolicy) { this.notBeforePolicy = notBeforePolicy; }
        public void setScope(String scope) { this.scope = scope; }
    }

    // Cached token wrapper
    private static class CachedToken {
        private final String accessToken;
        private final Instant expiresAt;

        public CachedToken(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
} 