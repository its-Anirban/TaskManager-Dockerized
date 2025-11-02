package com.example.taskManager.unit.security;

import com.example.taskManager.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = spy(new JwtUtil());
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
        doThrow(new JwtException("Malformed")).when(jwtUtil).extractAllClaims("bad_token");

        boolean result = jwtUtil.validateToken("bad_token");

        assertFalse(result, "Expected validateToken() to return false for malformed token");
        verify(jwtUtil).extractAllClaims("bad_token");
    }

    @Test
    void validateTokenWithUsername_shouldReturnFalse_whenExtractionFails() {
        doThrow(new RuntimeException("decode failed")).when(jwtUtil).extractUsername("broken_token");

        boolean result = jwtUtil.validateToken("broken_token", "user");

        assertFalse(result, "Expected validateToken(token, username) to return false when extraction fails");
        verify(jwtUtil).extractUsername("broken_token");
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
                        .setSubject("tokenUser") // mismatch username
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
