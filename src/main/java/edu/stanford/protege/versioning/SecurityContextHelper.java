package edu.stanford.protege.versioning;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityContextHelper {

    public static ExecutionContext getExecutionContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;

            Jwt jwt = jwtAuthToken.getToken();
            String userId = jwt.getClaimAsString("preferred_username");
            return new ExecutionContext(UserId.valueOf(userId), jwt.getTokenValue());
        }

        return null;
    }
}
