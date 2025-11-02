package com.example.taskManager.unit.config;

import com.example.taskManager.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class JwtPropertiesTest {

    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key");
        jwtProperties.setExpirationMs(7200000L);
        jwtProperties.setIssuer("test-issuer");
    }

    @Test
    void shouldStoreAndRetrieveSecretCorrectly() {
        assertThat(jwtProperties.getSecret()).isEqualTo("test-secret-key");
    }

    @Test
    void shouldStoreAndRetrieveExpirationMsCorrectly() {
        assertThat(jwtProperties.getExpirationMs()).isEqualTo(7200000L);
    }

    @Test
    void shouldStoreAndRetrieveIssuerCorrectly() {
        assertThat(jwtProperties.getIssuer()).isEqualTo("test-issuer");
    }

    @Test
    void shouldHaveValidDefaultValuesWhenUnset() {
        JwtProperties defaults = new JwtProperties();

        // Defensive null checks for wrapper Long fields
        assertThat(defaults.getSecret()).isNull();
        assertThat(defaults.getExpirationMs()).isNull();
        assertThat(defaults.getIssuer()).isNull();
    }

    @Test
    void shouldHaveReadableToStringOutput() {
        String result = jwtProperties.toString();

        // If toString() uses Lombok, it will include field values automatically
        assertThat(result)
                .contains("test-secret-key")
                .contains("7200000")
                .contains("test-issuer");
    }
}
