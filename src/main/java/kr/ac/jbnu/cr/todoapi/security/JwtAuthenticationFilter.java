package kr.ac.jbnu.cr.todoapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Comme dans le cours : resolveToken -> getUser -> setAuthentication
        resolveToken(request)
                .ifPresent(token -> jwtService.getUser(token)
                        .ifPresent(userId -> {
                            JwtAuthentication authentication = new JwtAuthentication(userId, token);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        })
                );

        filterChain.doFilter(request, response);
    }

    /**
     * Extract Bearer token from Authorization header
     */
    private Optional<String> resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }
}