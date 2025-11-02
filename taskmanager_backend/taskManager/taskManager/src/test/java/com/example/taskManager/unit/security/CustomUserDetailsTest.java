package com.example.taskManager.unit.security;

import com.example.taskManager.model.User;
import com.example.taskManager.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for CustomUserDetails")
class CustomUserDetailsTest {

    private CustomUserDetails customUserDetails;
    private User user;

    @BeforeEach
    void setUp() {
        // Create a User using your existing constructor (username, password)
        user = new User("demoUser", "demoPass");
        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("Should return correct username and password")
    void shouldReturnUsernameAndPasswordCorrectly() {
        assertEquals("demoUser", customUserDetails.getUsername());
        assertEquals("demoPass", customUserDetails.getPassword());
    }

    @Test
    @DisplayName("Should return empty list of authorities")
    void shouldReturnEmptyAuthoritiesList() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty(), "Expected authorities list to be empty");
    }

    @Test
    @DisplayName("Should always return true for account status checks")
    void shouldAlwaysReturnTrueForAccountChecks() {
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isEnabled());
    }

    @Test
    @DisplayName("Should return the same underlying User object")
    void shouldReturnOriginalUserObject() {
        assertSame(user, customUserDetails.getUser());
    }

    @Test
    @DisplayName("Should generate a valid toString() output")
    void shouldHaveReadableToStringOutput() {
        String result = customUserDetails.toString();
        assertNotNull(result);
        assertTrue(result.contains("demoUser"), "toString() should include username");
    }
}
