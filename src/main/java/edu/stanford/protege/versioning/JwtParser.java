package edu.stanford.protege.versioning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class JwtParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Jwt parseJwt(String token) throws Exception {
        String[] parts = token.split("\\.");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token format.");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String claimsJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        Map<String, Object> headers = objectMapper.readValue(headerJson, Map.class);
        Map<String, Object> claims = objectMapper.readValue(claimsJson, Map.class);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(3600); // or read from "exp" claim if present

        return new Jwt(token, issuedAt, expiresAt, headers, claims);
    }
}
