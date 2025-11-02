package com.example.taskManager.unit.services;

import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.security.CustomUserDetails;
import com.example.taskManager.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByUsername_WhenUserExists() {
        // Arrange
        User mockUser = new User(1L, "demoUser", "password123", false, Collections.emptyList());
        when(userRepository.findByUsername("demoUser")).thenReturn(Optional.of(mockUser));

        // Act
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername("demoUser");

        // Assert
        assertEquals("demoUser", details.getUsername());
        assertEquals("password123", details.getPassword());
        verify(userRepository, times(1)).findByUsername("demoUser");
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("ghost"));

        verify(userRepository, times(1)).findByUsername("ghost");
    }
}
