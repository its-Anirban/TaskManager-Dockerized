package com.example.taskManager.unit.config;

import com.example.taskManager.config.SecurityConfig;
import com.example.taskManager.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void shouldCreatePasswordEncoderBean() {
        assertThat(securityConfig.passwordEncoder()).isNotNull();
    }

    @Test
    void shouldCreateCorsConfigurationSourceBean() {
        assertThat(securityConfig.corsConfigurationSource()).isNotNull();
    }

    @Test
    void shouldCreateAuthenticationManagerBean() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertThat(result).isNotNull();
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

}
