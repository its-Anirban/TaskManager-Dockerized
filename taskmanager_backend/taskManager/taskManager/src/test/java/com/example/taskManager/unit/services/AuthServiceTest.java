package com.example.taskManager.unit.services;

import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.security.JwtUtil;
import com.example.taskManager.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

        private UserRepository userRepository;
        private JwtUtil jwtUtil;
        private PasswordEncoder passwordEncoder;
        private AuthService authService;

        @BeforeEach
        void setup() {
                userRepository = mock(UserRepository.class);
                jwtUtil = new JwtUtil();
                passwordEncoder = mock(PasswordEncoder.class);
                authService = new AuthService(userRepository, jwtUtil, passwordEncoder);

                // Common stubbing for password encoder
                when(passwordEncoder.encode(anyString()))
                                .thenAnswer(inv -> "hashed_" + inv.getArgument(0));
                when(passwordEncoder.matches(anyString(), anyString()))
                                .thenAnswer(inv -> {
                                        String raw = inv.getArgument(0);
                                        String hashed = inv.getArgument(1);
                                        return hashed.contains(raw); // simplistic match simulation
                                });
        }

        @Test
        void shouldLoginSuccessfully() {
                User user = new User("demoUser", "hashed_1234");
                user.setLoggedIn(false);

                when(userRepository.findByUsername("demoUser"))
                                .thenReturn(Optional.of(user));

                String token = authService.login("demoUser", "1234");

                assertNotNull(token, "JWT token should not be null after successful login");
                assertTrue(jwtUtil.validateToken(token, "demoUser"), "Token should be valid for the username");
                verify(userRepository).findByUsername("demoUser");
                verify(userRepository).save(user);
                assertTrue(user.isLoggedIn(), "User should be marked as logged in after login");
        }

        @Test
        void shouldThrowWhenInvalidPassword() {
                User user = new User("demoUser", "hashed_1234");
                when(userRepository.findByUsername("demoUser"))
                                .thenReturn(Optional.of(user));

                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> authService.login("demoUser", "wrong"));

                assertTrue(ex.getMessage().toLowerCase().contains("invalid"),
                                "Error message should indicate invalid credentials");
                verify(userRepository).findByUsername("demoUser");
                verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenUserNotFoundDuringLogin() {
                when(userRepository.findByUsername("ghostUser"))
                                .thenReturn(Optional.empty());

                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> authService.login("ghostUser", "pass"));

                // Your AuthService actually throws “Invalid username or password”
                assertTrue(
                                ex.getMessage().toLowerCase().contains("invalid"),
                                "Error message should indicate invalid credentials or user not found");
                verify(userRepository).findByUsername("ghostUser");
        }

        @Test
        void shouldLogoutSuccessfully() {
                User user = new User("demoUser", "hashed_1234");
                user.setLoggedIn(true);

                when(userRepository.findByUsername("demoUser"))
                                .thenReturn(Optional.of(user));

                authService.logout("demoUser");

                verify(userRepository).findByUsername("demoUser");
                verify(userRepository).save(user);
                assertFalse(user.isLoggedIn(), "User should be marked as logged out after logout");
        }

        @Test
        void shouldThrowWhenUserNotFoundDuringLogout() {
                when(userRepository.findByUsername("ghostUser"))
                                .thenReturn(Optional.empty());

                RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.logout("ghostUser"));

                assertTrue(ex.getMessage().toLowerCase().contains("not found"),
                                "Error message should indicate user not found");
                verify(userRepository).findByUsername("ghostUser");
                verify(userRepository, never()).save(any());
        }

        @Test
        void shouldRegisterNewUserSuccessfully() {
                when(userRepository.existsByUsername("newUser")).thenReturn(false);
                when(userRepository.save(any(User.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                User result = authService.register("newUser", "1234");

                assertNotNull(result, "Returned user should not be null");
                assertEquals("newUser", result.getUsername());
                assertTrue(result.getPassword().startsWith("hashed_"), "Password should be encoded");
                assertFalse(result.isLoggedIn(), "Newly registered user should not be logged in");
                verify(userRepository).existsByUsername("newUser");
                verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowWhenUsernameAlreadyExists() {
                when(userRepository.existsByUsername("existingUser")).thenReturn(true);

                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> authService.register("existingUser", "1234"));

                assertTrue(ex.getMessage().contains("exists"), "Message should indicate duplicate username");
                verify(userRepository).existsByUsername("existingUser");
                verify(userRepository, never()).save(any());
        }

}
