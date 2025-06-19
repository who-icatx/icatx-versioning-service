package edu.stanford.protege.versioning;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;

import java.util.UUID;

/**
 * Helper class to create ExecutionContext for scheduled operations using Keycloak client credentials
 */
public class KeycloakExecutionContextHelper {

    /**
     * Creates an ExecutionContext for scheduled operations using a service account
     * @param accessToken The Keycloak access token
     * @return ExecutionContext configured for the service account
     */
    public static ExecutionContext createServiceExecutionContext(String accessToken) {
        String correlationId = CorrelationMDCUtil.getCorrelationId() == null ? 
            UUID.randomUUID().toString() : CorrelationMDCUtil.getCorrelationId();
        
        // Use a service account user ID for scheduled operations
        return new ExecutionContext(
            UserId.valueOf("service-account-icatx-application"), 
            accessToken, 
            correlationId
        );
    }
} 