package com.example.taskManager.integration.config;

public class TestConfiguration {

    // Base URL for integration tests (backend running in Docker or locally)
    public static final String BASE_URL = "http://localhost:8080";

    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String TASKS = "/api/tasks";

}
