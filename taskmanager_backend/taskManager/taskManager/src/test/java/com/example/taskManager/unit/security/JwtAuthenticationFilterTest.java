package com.example.taskManager.unit.security;

import com.example.taskManager.security.JwtAuthenticationFilter;
import com.example.taskManager.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    private HttpServletRequest mockRequest(String header) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn(header);
        when(req.getRequestURI()).thenReturn("/api/tasks");
        when(req.getMethod()).thenReturn("GET");
        return req;
    }

    private HttpServletResponse mockResponse() throws Exception {
        HttpServletResponse res = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(res.getWriter()).thenReturn(pw);
        return res;
    }

    @Test
    void shouldAuthenticateUserWhenValidToken() throws Exception {
        String token = "valid.token";
        HttpServletRequest req = mockRequest("Bearer " + token);
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);
        var user = new User("testUser", "pass", Collections.emptyList());

        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(user);
        when(jwtUtil.validateToken(token, "testUser")).thenReturn(true);

        filter.invokeFilterForTest(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testUser", ((UsernamePasswordAuthenticationToken) auth).getName());
        verify(chain).doFilter(req, res);
    }

    @Test
    void shouldSkipWhenNoAuthorizationHeader() throws Exception {
        HttpServletRequest req = mockRequest(null);
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.invokeFilterForTest(req, res, chain);

        // Expect NO doFilter since response is 401
        verify(chain, never()).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipWhenHeaderDoesNotStartWithBearer() throws Exception {
        HttpServletRequest req = mockRequest("Token something");
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.invokeFilterForTest(req, res, chain);

        verify(chain, never()).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenTokenInvalid() throws Exception {
        String token = "invalid.token";
        HttpServletRequest req = mockRequest("Bearer " + token);
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);
        var user = new User("testUser", "pass", Collections.emptyList());

        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(user);
        when(jwtUtil.validateToken(token, "testUser")).thenReturn(false);

        filter.invokeFilterForTest(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void shouldHandleExceptionDuringUserLoadGracefully() throws Exception {
        String token = "crash.token";
        HttpServletRequest req = mockRequest("Bearer " + token);
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractUsername(token)).thenReturn("crashUser");
        when(userDetailsService.loadUserByUsername("crashUser"))
                .thenThrow(new RuntimeException("DB error"));
        when(jwtUtil.validateToken(token, "crashUser")).thenReturn(true);

        assertDoesNotThrow(() -> filter.invokeFilterForTest(req, res, chain));
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void shouldTriggerJwtExtractionCatchBlock() throws Exception {
        HttpServletRequest req = mockRequest("Bearer badtoken");
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new RuntimeException("Decode failed")).when(jwtUtil).extractUsername("badtoken");

        filter.invokeFilterForTest(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void shouldSkipWhenAuthenticationAlreadyExists() throws Exception {
        String token = "existing.token";
        HttpServletRequest req = mockRequest("Bearer " + token);
        HttpServletResponse res = mockResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractUsername(token)).thenReturn("user");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList())
        );

        filter.invokeFilterForTest(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
