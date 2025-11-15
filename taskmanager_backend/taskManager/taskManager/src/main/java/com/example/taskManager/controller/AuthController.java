package com.example.taskManager.controller;

import com.example.taskManager.dto.LoginRequest;
import com.example.taskManager.dto.LoginResponse;
import com.example.taskManager.dto.RegisterRequest;
import com.example.taskManager.model.User;
import com.example.taskManager.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String ERROR_KEY = "error";
    private static final String SUCCESS_MESSAGE = "message";
    private static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid RegisterRequest request) {
        try {
            User newUser = authService.register(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(SUCCESS_MESSAGE, "User registered successfully", "username", newUser.getUsername()));
        } catch (RuntimeException e) {
            // Handle known bad request issues
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, e.getMessage()));
        } catch (Exception e) {
            // Catch unexpected failures like DB down, etc.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, INTERNAL_ERROR_MESSAGE));
        }
    }

    /**
     * Log in user and return JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginRequest request) {
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(ERROR_KEY, INTERNAL_ERROR_MESSAGE));
        }
    }

    /**
     * Logout user.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            if (username == null || username.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(ERROR_KEY, "Username is required"));
            }

            authService.logout(username);
            return ResponseEntity.ok(Map.of(SUCCESS_MESSAGE, "User logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, INTERNAL_ERROR_MESSAGE));
        }
    }
}
