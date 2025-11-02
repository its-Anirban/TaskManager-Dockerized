package com.example.taskManager.integration.security;

import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String registerUrl = "/api/auth/register";
    private final String loginUrl = "/api/auth/login";
    private final String protectedUrl = "/api/tasks";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userRepository.save(new User("demoUser", "demoPass"));
    }

    @Test
    void shouldAllowPublicAccessToRegisterAndLogin() throws Exception {
        // Register endpoint
        var registerPayload = new RegisterRequest("newUser", "pass123");
        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isOk());

        // Login endpoint
        var loginPayload = new LoginRequest("demoUser", "demoPass");
        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get(protectedUrl))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToProtectedEndpointWithValidJwt() throws Exception {
        // First login to get a valid JWT
        var loginPayload = new LoginRequest("demoUser", "demoPass");

        var loginResult = mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        assertThat(token).isNotBlank();

        // Use JWT to access protected resource
        mockMvc.perform(get(protectedUrl)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectRequestWithInvalidJwt() throws Exception {
        mockMvc.perform(get(protectedUrl)
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }

    // === DTOs for request payloads ===
    static class LoginRequest {
        public String username;
        public String password;
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    static class RegisterRequest {
        public String username;
        public String password;
        public RegisterRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
