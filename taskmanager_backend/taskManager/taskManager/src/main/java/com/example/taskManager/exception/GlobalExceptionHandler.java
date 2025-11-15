package com.example.taskManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildError(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("not found")) {
            return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
        } else if (msg.contains("invalid") || msg.contains("unauthorized")) {
            // Login failure
            return buildError(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        } else if (
                msg.contains("exists") ||
                msg.contains("failed") ||
                msg.contains("db down") ||
                msg.contains("db failure") ||
                msg.contains("internal server error") ||
                msg.contains("logout failed") ||
                msg.contains("username already exists")
        ) {
            // Map all DB or process errors to 400
            return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } else {
            // Anything else becomes 500
            return buildError("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildError("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
