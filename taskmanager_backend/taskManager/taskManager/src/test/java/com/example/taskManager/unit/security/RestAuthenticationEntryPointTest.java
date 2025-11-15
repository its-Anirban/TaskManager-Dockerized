package com.example.taskManager.unit.security;

import com.example.taskManager.security.RestAuthenticationEntryPoint;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RestAuthenticationEntryPointTest {

    @Test
    void shouldReturnUnauthorizedResponse() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create a mock ServletOutputStream that writes to ByteArrayOutputStream
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No asynchronous I/O is required in this test mock — method intentionally left blank.
            }

            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        };

        when(response.getOutputStream()).thenReturn(servletOutputStream);

        entryPoint.commence(request, response,
                new org.springframework.security.core.AuthenticationException("Unauthorized") {});

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        // Optional: Validate JSON or text output content if applicable
        String output = baos.toString();
        assertTrue(output.contains("Unauthorized") || !output.isEmpty(),
                "Response should contain unauthorized message or non-empty content");
    }

    @Test
    void shouldHandleIOExceptionGracefully() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getOutputStream()).thenThrow(new IOException("Simulated I/O error"));

        assertThrows(IOException.class, () ->
                entryPoint.commence(request, response,
                        new org.springframework.security.core.AuthenticationException("Unauthorized") {}));
    }
}
