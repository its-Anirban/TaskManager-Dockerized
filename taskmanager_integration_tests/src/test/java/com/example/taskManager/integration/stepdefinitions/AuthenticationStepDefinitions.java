package com.example.taskManager.integration.stepdefinitions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.taskManager.integration.config.TestConfiguration;
import com.example.taskManager.integration.support.ApiClient;
import com.example.taskManager.integration.support.AssertionHelper;
import com.example.taskManager.integration.support.JsonHelper;
import com.example.taskManager.integration.support.TestContext;
import com.example.taskManager.integration.support.TokenStorage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationStepDefinitions {

    private String randomUser() {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private String randomPass() {
        return "pw_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Given("the authentication service is available")
    public void the_authentication_service_is_available() {

        List<String> probes = Arrays.asList(
                "/actuator/health",
                "/health",
                "/api/health",
                "/",
                "/api"
        );

        Response ok = null;
        String attempted = "";

        for (String p : probes) {
            attempted += p + " ";
            try {
                Response r = ApiClient.get(p, null);
                if (r != null && r.getStatusCode() == 200) {
                    ok = r;
                    break;
                }
            } catch (Exception ignored) {}
        }

        assertNotNull(ok, "Backend not healthy. Tried: " + attempted);
        TestContext.put("auth.health", true);
    }

    @When("I register a user with username {string} and password {string}")
    public void i_register_a_user_with_username_and_password(String u, String p) {

        String username =
                (u == null || u.isBlank() || u.equalsIgnoreCase("random"))
                        ? randomUser()
                        : u;

        String password =
                (p == null || p.isBlank() || p.equalsIgnoreCase("random"))
                        ? randomPass()
                        : p;

        TestContext.put("username", username);
        TestContext.put("password", password);

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Response r = ApiClient.post(TestConfiguration.AUTH_REGISTER, JsonHelper.toJson(body), null);
        TestContext.setLastResponse(r);
    }

    @When("I login with username {string} and password {string}")
    public void i_login_with_username_and_password(String u, String p) {

        String username = u.equalsIgnoreCase("context")
                ? TestContext.get("username")
                : u;

        String password = p.equalsIgnoreCase("context")
                ? TestContext.get("password")
                : p;

        assertNotNull(username, "Username missing");
        assertNotNull(password, "Password missing");

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Response r = ApiClient.post(TestConfiguration.AUTH_LOGIN, JsonHelper.toJson(body), null);
        TestContext.setLastResponse(r);

        if (r != null && r.getStatusCode() == 200) {
            String token = r.jsonPath().getString("token");
            if (token != null) TokenStorage.setToken(token);
        }
    }

    @Given("I am logged in as {string} with password {string}")
    public void i_am_logged_in_as_with_password(String u, String p) {

        i_login_with_username_and_password(u, p);

        Response r = TestContext.getLastResponse();
        assertEquals(200, r.getStatusCode(), "Login must return 200");

        String token = r.jsonPath().getString("token");
        assertNotNull(token, "Token must not be null");

        TokenStorage.setToken(token);
    }

    @Then("the response should contain a {string}")
    public void the_response_should_contain(String key) {
        AssertionHelper.hasJsonKey(TestContext.getLastResponse(), key);
    }

    // 🔥 NEWLY ADDED MISSING STEP
    @Then("the response should contain {string}")
    public void the_response_should_contain_key_only(String key) {
        Response r = TestContext.getLastResponse();
        assertNotNull(r, "No response available");

        Object value = r.jsonPath().get(key);
        assertNotNull(value, "Expected JSON key missing: " + key);
    }

    @Then("the response should contain {string} with value {string}")
    public void the_response_should_contain_with_value(String key, String value) {
        Response r = TestContext.getLastResponse();
        assertNotNull(r, "No response available");
        assertEquals(value, r.jsonPath().getString(key));
    }
}
