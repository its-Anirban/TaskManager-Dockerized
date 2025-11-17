package com.example.taskManager.integration.exception;

import com.example.taskManager.exception.InvalidCredentialsException;
import com.example.taskManager.exception.UserAlreadyExistsException;
import com.example.taskManager.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerExtendedIT.TestExceptionController.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerExtendedIT {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestExceptionController {

        @GetMapping("/test/runtime")
        public void runtime(@RequestParam("msg") String msg) {
            throw new RuntimeException(msg);
        }

        @GetMapping("/test/invalid-credentials")
        public void invalidCredentials() {
            throw new InvalidCredentialsException("invalid credentials provided");
        }

        @GetMapping("/test/user-not-found")
        public void userNotFound() {
            throw new UserNotFoundException("user not found in system");
        }

        @GetMapping("/test/user-exists")
        public void userExists() {
            throw new UserAlreadyExistsException("User already exists");
        }

        @GetMapping("/test/checked")
        public void checked() throws Exception {
            throw new Exception("checked-exception");
        }
    }

    // ============================
    //  RuntimeException Branches
    // ============================

    @Test
    void msgContainsNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/runtime").param("msg", "record not found in db"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("record not found in db")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void msgContainsInvalidOrUnauthorized_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/runtime").param("msg", "invalid login data"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("invalid login data")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        mockMvc.perform(get("/test/runtime").param("msg", "unauthorized access attempt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized access attempt")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void dbError_keywords_shouldReturn400_forAllKeywords() throws Exception {

        String[] messages = {
                "record already exists",
                "operation failed unexpectedly",
                "db down during transaction",
                "db failure on update",
                "internal server error inside db module",
                "logout failed while clearing session",
                "username already exists in database"
        };

        for (String msg : messages) {
            mockMvc.perform(get("/test/runtime").param("msg", msg))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is(msg)))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Test
    void msgNotMatchingAnything_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/runtime").param("msg", "random unmatched message"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Internal server error")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // ============================
    //  Custom Exception Handlers
    // ============================

    @Test
    void invalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/invalid-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("invalid credentials provided")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void userNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("user not found in system")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void userAlreadyExists_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/user-exists"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User already exists")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // ============================
    //  Generic Exception Handler
    // ============================

    @Test
    void checkedException_shouldReturn500_genericMessage() throws Exception {
        mockMvc.perform(get("/test/checked"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Unexpected error occurred")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
