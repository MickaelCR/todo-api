package kr.ac.jbnu.cr.todoapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.jbnu.cr.todoapi.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for the REST API.
 * Returns responses following RFC 9457 (Problem Details for HTTP APIs).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle 404 Not Found - Todo not found
     */
    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTodoNotFound(TodoNotFoundException ex,
                                                            HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        logger.warn("[{}] Todo not found: {}", requestId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Not Found")
                .status(404)
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle 409 Conflict - Todo already completed
     */
    @ExceptionHandler(TodoAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handleTodoAlreadyCompleted(TodoAlreadyCompletedException ex,
                                                                    HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        logger.warn("[{}] Conflict: {}", requestId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Conflict")
                .status(409)
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle 400 Bad Request - Validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });

        logger.warn("[{}] Validation error: {}", requestId, fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Invalid Request")
                .status(400)
                .detail("One or more fields are invalid.")
                .instance(request.getRequestURI())
                .errors(fieldErrors)
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle 400 Bad Request - Malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        logger.warn("[{}] Malformed JSON: {}", requestId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Bad Request")
                .status(400)
                .detail("Request body is missing or malformed.")
                .instance(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        logger.warn("[{}] Method not allowed: {}", requestId, ex.getMethod());

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Method Not Allowed")
                .status(405)
                .detail("HTTP method " + ex.getMethod() + " is not supported for this endpoint.")
                .instance(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    /**
     * Handle 404 Not Found - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        String requestId = UUID.randomUUID().toString();

        logger.warn("[{}] No handler found: {} {}", requestId, ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Not Found")
                .status(404)
                .detail("Endpoint " + ex.getRequestURL() + " not found.")
                .instance(ex.getRequestURL())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle 500 Internal Server Error - Catch-all
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        logger.error("[{}] Internal server error: {}", requestId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .type("about:blank")
                .title("Internal Server Error")
                .status(500)
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}