package com.example.taskManager.unit.controller;

import com.example.taskManager.controller.AuthController;
import com.example.taskManager.dto.LoginRequest;
import com.example.taskManager.dto.LoginResponse;
import com.example.taskManager.dto.RegisterRequest;
import com.example.taskManager.model.User;
import com.example.taskManager.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ====== LOGIN TESTS ======

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demoUser");
        request.setPassword("demoPass");

        when(authService.login("demoUser", "demoPass")).thenReturn("mock-jwt-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);
        assertEquals("mock-jwt-token", ((LoginResponse) response.getBody()).getToken());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("wrongUser");
        request.setPassword("wrongPass");

        when(authService.login(any(), any()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("error"));
        assertNotNull(body.get("error"));
        assertTrue(body.get("error").toString().contains("Invalid credentials"));
    }

    @Test
    void shouldHandleGenericExceptionDuringLogin() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demoUser");
        request.setPassword("demoPass");

        // Simulate a generic runtime failure inside the service
        when(authService.login(any(), any())).thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<?> response = authController.login(request);

        // Controller maps runtime exceptions for login to 401 (observed behavior)
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        // Verify structure rather than exact message text
        assertTrue(body.containsKey("error"));
        assertNotNull(body.get("error"));
        assertFalse(body.get("error").toString().isEmpty());
    }

    // ====== REGISTER TESTS ======

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("newPass");

        User mockUser = new User();
        mockUser.setUsername("newUser");
        when(authService.register("newUser", "newPass")).thenReturn(mockUser);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("username"));
        assertEquals("newUser", body.get("username"));
    }

    @Test
    void shouldReturnBadRequestOnRuntimeExceptionDuringRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateUser");
        request.setPassword("1234");

        when(authService.register(any(), any()))
                .thenThrow(new RuntimeException("Username already exists"));

        ResponseEntity<?> response = authController.register(request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("error"));
        assertNotNull(body.get("error"));
        assertTrue(body.get("error").toString().contains("Username already exists"));
    }

    @Test
    void shouldHandleGenericExceptionDuringRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("pass");

        when(authService.register(any(), any())).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<?> response = authController.register(request);

        // Controller maps runtime exceptions for register to 400 (observed behavior)
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        // Verify an error key exists and is non-empty (don't rely on exact wording)
        assertTrue(body.containsKey("error"));
        assertNotNull(body.get("error"));
        assertFalse(body.get("error").toString().isEmpty());
    }

    // ====== LOGOUT TESTS ======

    @Test
    void shouldLogoutSuccessfully() {
        Map<String, String> body = Map.of("username", "demoUser");

        ResponseEntity<?> response = authController.logout(body);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> resp = (Map<?, ?>) response.getBody();
        assertTrue(resp.containsKey("message"));
        verify(authService).logout("demoUser");
    }

    @Test
    void shouldReturnBadRequestIfUsernameMissing() {
        Map<String, String> body = Map.of();

        ResponseEntity<?> response = authController.logout(body);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> resp = (Map<?, ?>) response.getBody();
        assertTrue(resp.containsKey("error"));
        assertNotNull(resp.get("error"));
        assertFalse(resp.get("error").toString().isEmpty());
    }

    @Test
    void shouldReturnBadRequestOnRuntimeExceptionDuringLogout() {
        Map<String, String> body = Map.of("username", "badUser");

        doThrow(new RuntimeException("Logout failed"))
                .when(authService).logout("badUser");

        ResponseEntity<?> response = authController.logout(body);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> resp = (Map<?, ?>) response.getBody();
        assertTrue(resp.containsKey("error"));
        assertNotNull(resp.get("error"));
        assertTrue(resp.get("error").toString().contains("Logout failed"));
    }

    @Test
    void shouldHandleGenericExceptionDuringLogout() {
        Map<String, String> body = Map.of("username", "demoUser");

        doThrow(new RuntimeException("Internal server error")).when(authService).logout("demoUser");

        ResponseEntity<?> response = authController.logout(body);

        // controller returns 400 for runtime logout errors (observed)
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Map<?, ?> resp = (Map<?, ?>) response.getBody();
        assertTrue(resp.containsKey("error"));
        assertNotNull(resp.get("error"));
        assertFalse(resp.get("error").toString().isEmpty());
    }
}
