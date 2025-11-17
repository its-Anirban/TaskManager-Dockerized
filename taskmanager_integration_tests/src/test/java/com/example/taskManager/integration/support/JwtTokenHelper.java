package com.example.taskManager.integration.support;

public class JwtTokenHelper {

    public String extractToken(String token) {
        // The backend already sends a ready JWT.
        // No need to decode it for integration tests.
        return token;
    }
}
