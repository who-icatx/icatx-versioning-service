package edu.stanford.protege.versioning.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakService, "tokenUrl", "http://test-keycloak/token");
        ReflectionTestUtils.setField(keycloakService, "clientId", "test-client");
        ReflectionTestUtils.setField(keycloakService, "clientSecret", "test-secret");
    }

    @Test
    void shouldReturnAccessToken() {
        // Given
        KeycloakService.TokenResponse mockResponse = new KeycloakService.TokenResponse();
        mockResponse.setAccessToken("test-access-token");
        mockResponse.setExpiresIn(300);
        mockResponse.setTokenType("Bearer");

        ResponseEntity<KeycloakService.TokenResponse> responseEntity = 
            new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(eq("http://test-keycloak/token"), any(HttpEntity.class), eq(KeycloakService.TokenResponse.class)))
            .thenReturn(responseEntity);

        // When
        String accessToken = keycloakService.getAccessToken();

        // Then
        assertNotNull(accessToken);
        assertEquals("test-access-token", accessToken);
    }

    @Test
    void shouldThrowExceptionWhenTokenRequestFails() {
        // Given
        when(restTemplate.postForEntity(eq("http://test-keycloak/token"), any(HttpEntity.class), eq(KeycloakService.TokenResponse.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> keycloakService.getAccessToken());
        assertEquals("Failed to authenticate with Keycloak", exception.getMessage());
    }
} 