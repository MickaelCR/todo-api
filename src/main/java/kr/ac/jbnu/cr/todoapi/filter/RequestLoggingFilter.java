package kr.ac.jbnu.cr.todoapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Middleware filter for logging HTTP requests and responses.
 * Follows the Filter pattern as shown in JWT course material.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();

        // Capture start time
        long startTime = System.currentTimeMillis();
        String timestamp = Instant.now().toString();

        // Extract request information
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        // Log incoming request
        logger.info("==> [{}] {} {} | IP: {} | Time: {}",
                requestId,
                method,
                queryString != null ? uri + "?" + queryString : uri,
                clientIp,
                timestamp);

        // Add request ID to response header for tracing
        response.setHeader("X-Request-ID", requestId);

        try {
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Log outgoing response
            logger.info("<== [{}] {} {} | Status: {} | Duration: {}ms",
                    requestId,
                    method,
                    uri,
                    status,
                    duration);
        }
    }

    /**
     * Extract client IP address from request
     * Handles proxy headers (X-Forwarded-For)
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP if multiple are present
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}