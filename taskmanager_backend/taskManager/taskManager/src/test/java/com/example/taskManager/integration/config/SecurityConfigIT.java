package com.example.taskManager.integration.config;

import com.example.taskManager.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests to exercise SecurityConfig.securityFilterChain(...) behavior.
 * - Provides a mock JwtAuthenticationFilter bean so the SecurityConfig can be created.
 * - Adds a DummyController that exposes public and protected endpoints for GET/POST/DELETE.
 * - Stubs /h2-console and /error endpoints so permitAll branches are executed.
 *
 * This test will exercise:
 *   cors(...), csrf().disable(), headers().frameOptions().disable(),
 *   sessionManagement(SessionCreationPolicy.STATELESS),
 *   authorizeHttpRequests(... requestMatchers(...).permitAll() ... anyRequest().authenticated()),
 *   addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class),
 *   formLogin().disable(), httpBasic().disable()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIT {

    @Autowired
    private MockMvc mockMvc;

    // Provide a mock JwtAuthenticationFilter bean in the test context (stable alternative to @MockBean/@MockitoBean)
    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }
    }

    // ---------- INNER CONTROLLER FOR TEST COVERAGE ----------
    // Expose endpoints that the security rules mention and ensure the test methods use HTTP methods that exist.
    @RestController
    @RequestMapping("/api")
    static class DummyController {

        // public endpoint (permitAll)
        @RequestMapping(value = "/auth/hello", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
        public String authHello() {
            return "auth-ok";
        }

        // protected endpoint (should be authenticated)
        @RequestMapping(value = "/private/hello", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
        public String privateHello() {
            return "private-ok";
        }
    }

    // Provide small stubs for /h2-console and /error so permitAll() branches are exercised.
    @RestController
    static class ConsoleAndErrorController {
        @RequestMapping(value = "/h2-console", method = {RequestMethod.GET, RequestMethod.POST})
        public String h2Console() {
            return "h2";
        }

        @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
        public String error() {
            return "err";
        }
    }
    // ---------------------------------------------------------

    @Test
    void shouldPermitPublicAuthEndpoints() throws Exception {
        mockMvc.perform(get("/api/auth/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("auth-ok"));
    }

    @Test
    void shouldPermitH2ConsoleAndError() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk())
                .andExpect(content().string("h2"));

        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(content().string("err"));
    }

    @Test
    void shouldPermitOptionsRequests() throws Exception {
        mockMvc.perform(options("/anything")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectPrivateEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldTriggerAllFilterChainComponents() throws Exception {
        // GET public allowed
        mockMvc.perform(get("/api/auth/hello"))
                .andExpect(status().isOk());

        // GET private unauthorized
        mockMvc.perform(get("/api/private/hello"))
                .andExpect(status().isUnauthorized());

        // POST public allowed (CSRF disabled in config)
        mockMvc.perform(post("/api/auth/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":1}"))
                .andExpect(status().isOk());

        // DELETE private unauthorized
        mockMvc.perform(delete("/api/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldInitializeAndExecuteFullSecurityChain() throws Exception {
        // combination: options (CORS preflight), public get, private get, delete public, post private
        mockMvc.perform(options("/api/auth/hello")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/hello"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/private/hello"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/auth/hello"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/private/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"a\":1}"))
                .andExpect(status().isUnauthorized());
    }
}
