package edu.stanford.protege.versioning;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Helper class to create ExecutionContext for scheduled operations using Keycloak client credentials
 */
public class KeycloakExecutionContextHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(KeycloakExecutionContextHelper.class);

    /**
     * Creates an ExecutionContext for scheduled operations using a service account
     * @param accessToken The Keycloak access token
     * @return ExecutionContext configured for the service account
     */
    public static ExecutionContext createServiceExecutionContext(String accessToken) {
        String correlationId = CorrelationMDCUtil.getCorrelationId() == null ? 
            UUID.randomUUID().toString() : CorrelationMDCUtil.getCorrelationId();
        Jwt jwt;

        try {
            jwt = JwtParser.parseJwt(accessToken);
            String userId = jwt.getClaimAsString("preferred_username");
            return new ExecutionContext(UserId.valueOf(userId), jwt.getTokenValue(), correlationId);
        } catch (Exception e) {
            LOGGER.error("Couldn't parse JWT, trying the default user", e);

            return new ExecutionContext(
                    UserId.valueOf("service-account-icatx_application"),
                    accessToken,
                    correlationId
            );
        }
    }
} 