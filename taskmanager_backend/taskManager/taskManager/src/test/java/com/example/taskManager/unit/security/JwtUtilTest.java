package com.example.taskManager.unit.security;

import com.example.taskManager.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "DevelopmentOnlySecretKeyForTaskManager123456");

        Mockito.mock(JwtUtil.class);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("anirban");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, "anirban"));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtil.generateToken("user123");
        assertEquals("user123", jwtUtil.extractUsername(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("fakeToken", "user"));
    }

    @Test
    void shouldReturnFalseForDifferentUsername() {
        String token = jwtUtil.generateToken("realUser");
        assertFalse(jwtUtil.validateToken(token, "otherUser"));
    }

    @Test
    void shouldHandleMalformedTokenGracefully() {
        String malformed = "abc.def.ghi";
        assertFalse(jwtUtil.validateToken(malformed, "test"));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenMalformed() {
        JwtUtil spyJwt = spy(jwtUtil); // create a spy, not use real object

        doThrow(new JwtException("Malformed")).when(spyJwt).extractAllClaims("bad_token");

        boolean result = spyJwt.validateToken("bad_token");

        assertFalse(result, "Expected validateToken() to return false for malformed token");
        verify(spyJwt).extractAllClaims("bad_token");
    }

    @Test
    void validateTokenWithUsername_shouldReturnFalse_whenExtractionFails() {
        JwtUtil spyJwt = spy(jwtUtil);

        doThrow(new RuntimeException("decode failed")).when(spyJwt).extractUsername("broken_token");

        boolean result = spyJwt.validateToken("broken_token", "user");

        assertFalse(result, "Expected validateToken(token, username) to return false when extraction fails");
        verify(spyJwt).extractUsername("broken_token");
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenValid() {
        String token = jwtUtil.generateToken("validUser");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateTokenWithUsername_shouldReturnFalse_whenUserMatchButExpired() {
        JwtUtil shortExpiry = new JwtUtil() {
            @Override
            public String generateToken(String username) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(username)
                        .setExpiration(new java.util.Date(System.currentTimeMillis() - 2000))
                        .signWith(new javax.crypto.spec.SecretKeySpec(
                                "change-this-to-a-secure-32-char-min-secret-key!".getBytes(),
                                io.jsonwebtoken.SignatureAlgorithm.HS256.getJcaName()))
                        .compact();
            }
        };

        String expiredToken = shortExpiry.generateToken("sameUser");
        boolean result = shortExpiry.validateToken(expiredToken, "sameUser");

        assertFalse(result, "Expected false when username matches but token is expired");
    }

    @Test
    void validateTokenWithUsername_shouldReturnFalse_whenExpired() {
        JwtUtil shortExpiry = new JwtUtil() {
            @Override
            public String generateToken(String username) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(username)
                        .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                        .signWith(new javax.crypto.spec.SecretKeySpec(
                                "change-this-to-a-secure-32-char-min-secret-key!".getBytes(),
                                io.jsonwebtoken.SignatureAlgorithm.HS256.getJcaName()))
                        .compact();
            }
        };
        String expiredToken = shortExpiry.generateToken("expiredUser");
        assertFalse(shortExpiry.validateToken(expiredToken, "expiredUser"));
    }

    @Test
    void validateTokenWithUsername_shouldReturnFalse_whenUsernameMismatchAndTokenExpired() {
        JwtUtil shortExpiry = new JwtUtil() {
            @Override
            public String generateToken(String username) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject("tokenUser")
                        .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                        .signWith(new javax.crypto.spec.SecretKeySpec(
                                "change-this-to-a-secure-32-char-min-secret-key!".getBytes(),
                                io.jsonwebtoken.SignatureAlgorithm.HS256.getJcaName()))
                        .compact();
            }
        };

        String expiredToken = shortExpiry.generateToken("ignoredUser");
        boolean result = shortExpiry.validateToken(expiredToken, "expectedUser");

        assertFalse(result, "Expected false when username mismatch AND token expired");
    }
}
