package com.example.taskManager.integration.services;

import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.security.JwtUtil;
import com.example.taskManager.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        // Use the correct constructor from your User entity
        User demoUser = new User("demoUser", "demoPass");
        userRepository.save(demoUser);
    }

    @Test
    void shouldLoginSuccessfullyAndGenerateValidJwt() {
        String token = authService.login("demoUser", "demoPass");

        assertNotNull(token, "Token should not be null");
        assertTrue(jwtUtil.validateToken(token, "demoUser"),
                "Generated JWT should be valid for the given username");
    }

    @Test
    void shouldThrowWhenInvalidPassword() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("demoUser", "wrongPass"));

        assertTrue(ex.getMessage().toLowerCase().contains("invalid"),
                "Exception message should indicate invalid credentials");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("ghostUser", "randomPass"));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"),
                "Exception message should indicate user not found");
    }

    @Test
    void shouldRegisterNewUserInDatabase() {
        authService.register("newUser", "1234");

        assertTrue(userRepository.existsByUsername("newUser"), "User should be saved to DB");
    }

}
