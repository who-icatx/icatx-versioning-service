package edu.stanford.protege.gateway;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        var authorities = authoritiesConverter.convert(jwt);

        var realmRoles = (Collection<String>) jwt.getClaimAsMap("realm_access")
                .getOrDefault("roles", List.of());

        authorities.addAll(realmRoles.stream()
                .map(role -> "ROLE_" + role.toUpperCase()) // Prefix roles with "ROLE_" to match Spring Security conventions
                .map(SimpleGrantedAuthority::new)
                .toList()
        );

        return authorities;
    }
}