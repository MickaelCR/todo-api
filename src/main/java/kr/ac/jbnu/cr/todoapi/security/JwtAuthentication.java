package kr.ac.jbnu.cr.todoapi.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final Long userId;
    private final String token;

    public JwtAuthentication(Long userId, String token) {
        super(null);
        this.userId = userId;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public Long getUserId() {
        return userId;
    }
}