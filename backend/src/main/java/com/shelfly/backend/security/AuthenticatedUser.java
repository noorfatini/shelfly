package com.shelfly.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Lightweight authenticated principal built straight from JWT claims.
 * We deliberately avoid a DB round-trip on every request for speed;
 * the token itself carries id/email/role.
 */
@Getter
public class AuthenticatedUser extends UsernamePasswordAuthenticationToken {

    private final String userId;
    private final String email;

    public AuthenticatedUser(String userId, String email, String role) {
        super(userId, null, List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userId = userId;
        this.email = email;
    }
}
