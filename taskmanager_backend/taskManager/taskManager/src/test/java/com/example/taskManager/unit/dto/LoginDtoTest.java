package com.example.taskManager.unit.dto;

import com.example.taskManager.dto.LoginRequest;
import com.example.taskManager.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit tests for LoginRequest and LoginResponse DTOs")
class LoginDtoTest {

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginResponse = new LoginResponse();
    }

    // ---- LoginRequest ----
    @Test
    @DisplayName("LoginRequest should store and retrieve username and password")
    void shouldStoreAndRetrieveLoginRequestFields() {
        loginRequest.setUsername("demoUser");
        loginRequest.setPassword("demoPass");

        assertThat(loginRequest.getUsername()).isEqualTo("demoUser");
        assertThat(loginRequest.getPassword()).isEqualTo("demoPass");
    }

    @Test
    @DisplayName("LoginRequest all-args constructor should initialize fields correctly")
    void shouldInitializeLoginRequestUsingConstructor() {
        LoginRequest req = new LoginRequest("user123", "pass123");
        assertThat(req.getUsername()).isEqualTo("user123");
        assertThat(req.getPassword()).isEqualTo("pass123");
    }

    @Test
    @DisplayName("LoginRequest toString() should include username")
    void shouldHaveReadableToStringInLoginRequest() {
        loginRequest.setUsername("visibleUser");
        String toStringValue = loginRequest.toString();
        assertThat(toStringValue).contains("visibleUser");
    }

    // ---- LoginResponse ----
    @Test
    @DisplayName("LoginResponse should store and retrieve token")
    void shouldStoreAndRetrieveToken() {
        loginResponse.setToken("mock-jwt-token");
        assertThat(loginResponse.getToken()).isEqualTo("mock-jwt-token");
    }

    @Test
    @DisplayName("LoginResponse all-args constructor should initialize fields correctly")
    void shouldInitializeLoginResponseUsingConstructor() {
        LoginResponse resp = new LoginResponse("jwt-12345");
        assertThat(resp.getToken()).isEqualTo("jwt-12345");
    }

    @Test
    @DisplayName("LoginResponse toString() should include token")
    void shouldHaveReadableToStringInLoginResponse() {
        loginResponse.setToken("token-xyz");
        String toStringValue = loginResponse.toString();
        assertThat(toStringValue).contains("token-xyz");
    }
}
