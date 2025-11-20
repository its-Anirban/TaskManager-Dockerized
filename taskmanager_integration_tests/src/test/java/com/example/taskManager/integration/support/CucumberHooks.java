package com.example.taskManager.integration.support;

import com.example.taskManager.integration.config.TestConfiguration;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Cucumber hooks:
 * - Clears token & test context before each scenario.
 * - Performs a health-check.
 * - Ensures a known test account exists and logs it in (to avoid 401s).
 *
 * Note: This creates/ensures a test user "testuser"/"password123".
 * If you want per-scenario isolated users, we can change this to generate unique users.
 */
public class CucumberHooks {

    private static final String DEFAULT_USER = "testuser";
    private static final String DEFAULT_PASSWORD = "password123";

    @Before(order = 0)
    public void beforeScenarioClear(Scenario scenario) {
        TokenStorage.clear();
        TestContext.clear();
    }

    @Before(order = 1)
    public void beforeScenarioHealthAndAuth(Scenario scenario) {
        // 1) health check (try typical endpoints)
        boolean healthy = false;
        String[] probes = {"/actuator/health", "/health", "/api/health", "/", "/api"};
        for (String p : probes) {
            try {
                Response r = ApiClient.get(p, null);
                if (r != null && r.getStatusCode() == 200) {
                    healthy = true;
                    break;
                }
            } catch (Exception ex) {
                // ignore and try next probe
            }
        }
        if (!healthy) {
            throw new IllegalStateException("Backend service not reachable on expected health endpoints.");
        }

        // 2) Ensure the canonical test user exists and is logged in.
        // Try to register; if user already exists a 400 may be returned - ignore that.
        try {
            Map<String, Object> regBody = new HashMap<>();
            regBody.put("username", DEFAULT_USER);
            regBody.put("password", DEFAULT_PASSWORD);

            Response rr = ApiClient.post(TestConfiguration.AUTH_REGISTER, JsonHelper.toJson(regBody), null);
            int code = rr != null ? rr.getStatusCode() : 0;
            if (code != 201 && code != 400) {
                // 201 = created, 400 = already exists - both acceptable for our setup
                throw new IllegalStateException("Unexpected response when ensuring test user: " + code);
            }
        } catch (Exception e) {
            // If register fails in an unexpected way, surface it
            throw new RuntimeException("Failed to ensure test user exists: " + e.getMessage(), e);
        }

        // 3) Login the test user and store token
        try {
            Map<String, Object> loginBody = new HashMap<>();
            loginBody.put("username", DEFAULT_USER);
            loginBody.put("password", DEFAULT_PASSWORD);

            Response lr = ApiClient.post(TestConfiguration.AUTH_LOGIN, JsonHelper.toJson(loginBody), null);
            if (lr == null || lr.getStatusCode() != 200) {
                throw new IllegalStateException("Failed to login test user during hook; status: " +
                        (lr == null ? "null" : lr.getStatusCode()));
            }
            String token = lr.jsonPath().getString("token");
            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("Login returned no token for test user.");
            }
            TokenStorage.setToken(token);

            // also store credentials in context for feature steps that might reference them
            TestContext.put("auto.username", DEFAULT_USER);
            TestContext.put("auto.password", DEFAULT_PASSWORD);

        } catch (Exception e) {
            throw new RuntimeException("Failed to login test user in Cucumber hook: " + e.getMessage(), e);
        }
    }
}
