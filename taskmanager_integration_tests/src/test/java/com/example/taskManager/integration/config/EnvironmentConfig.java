package com.example.taskManager.integration.config;

import java.io.InputStream;

public class EnvironmentConfig {

    static {
        try (InputStream in = EnvironmentConfig.class.getResourceAsStream("/config/application-dev.yml")) {
            // simple placeholder - tests primarily use system properties or defaults
        } catch (Exception e) {
            // ignore
        }
    }

    public static String baseUrl() {
        return System.getProperty("base.url", "http://localhost:8080");
    }
}
