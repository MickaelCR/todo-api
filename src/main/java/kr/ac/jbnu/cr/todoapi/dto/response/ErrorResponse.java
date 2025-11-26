package kr.ac.jbnu.cr.todoapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Error response following RFC 9457 (Problem Details for HTTP APIs)
 * Content-Type: application/problem+json
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String type;
    private String title;
    private int status;
    private String detail;
    private Map<String, Object> errors;
    private String requestId;
}