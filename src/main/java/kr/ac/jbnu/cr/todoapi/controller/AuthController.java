package kr.ac.jbnu.cr.todoapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.jbnu.cr.todoapi.dto.request.LoginRequest;
import kr.ac.jbnu.cr.todoapi.dto.request.RegisterRequest;
import kr.ac.jbnu.cr.todoapi.dto.response.ApiResponse;
import kr.ac.jbnu.cr.todoapi.dto.response.ErrorResponse;
import kr.ac.jbnu.cr.todoapi.model.User;
import kr.ac.jbnu.cr.todoapi.security.JwtAuthentication;
import kr.ac.jbnu.cr.todoapi.security.JwtService;
import kr.ac.jbnu.cr.todoapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String requestId = UUID.randomUUID().toString();

        if (userService.existsByUsername(request.getUsername())) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Conflict")
                    .status(409)
                    .detail("Username '" + request.getUsername() + "' is already taken.")
                    .instance("/auth/register")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User user = userService.register(request);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());

        Map<String, String> links = new HashMap<>();
        links.put("login", "/auth/login");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, requestId, links));
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String requestId = UUID.randomUUID().toString();

        // Find user
        var userOptional = userService.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Unauthorized")
                    .status(401)
                    .detail("Invalid username or password.")
                    .instance("/auth/login")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userOptional.get();

        // Verify password (comme dans le cours)
        if (!userService.checkPassword(user, request.getPassword())) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Unauthorized")
                    .status(401)
                    .detail("Invalid username or password.")
                    .instance("/auth/login")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Generate token (comme dans le cours)
        String token = jwtService.createToken(user);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        Map<String, String> links = new HashMap<>();
        links.put("self", "/auth/login");
        links.put("me", "/auth/me");

        return ResponseEntity.ok(ApiResponse.success(data, requestId, links));
    }

    @Operation(summary = "Get current user info (requires authentication)")
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String requestId = UUID.randomUUID().toString();

        // Get authenticated user from SecurityContext
        JwtAuthentication authentication =
                (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Long userId = authentication.getUserId();

        var userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            ErrorResponse error = ErrorResponse.builder()
                    .type("about:blank")
                    .title("Not Found")
                    .status(404)
                    .detail("User not found.")
                    .instance("/auth/me")
                    .requestId(requestId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        User user = userOptional.get();

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());

        Map<String, String> links = new HashMap<>();
        links.put("self", "/auth/me");

        return ResponseEntity.ok(ApiResponse.success(data, requestId, links));
    }
}