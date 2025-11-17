package com.example.taskManager.integration.stepdefinitions;

import java.util.Map;

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

public class AuthenticationStepDefinitions {

    @Given("the authentication service is available")
    public void service_available() { }

    @When("I register a user with username {string} and password {string}")
    public void register_user(String username, String password) {
        Map<String,String> body = Map.of("username", username, "password", password);
        Response r = ApiClient.post(TestConfiguration.AUTH_REGISTER, JsonHelper.toJson(body), null);
        TestContext.setLastResponse(r);
    }

    @Then("the response status should be {int}")
    public void status_should_be(int code) {
        AssertionHelper.status(TestContext.getLastResponse(), code);
    }

    @Then("the response should contain {string} with value {string}")
    public void response_contains(String key, String value) {
        AssertionHelper.jsonValueEquals(TestContext.getLastResponse(), key, value);
    }

    @When("I login with username {string} and password {string}")
    public void login(String username, String password) {
        Map<String,String> body = Map.of("username", username, "password", password);
        Response r = ApiClient.post(TestConfiguration.AUTH_LOGIN, JsonHelper.toJson(body), null);
        TestContext.setLastResponse(r);

        if (r.getStatusCode() == 200) {
            String token = r.getBody().jsonPath().getString("token");
            TokenStorage.setToken(token);
        }
    }

    @Then("the response should contain a {string}")
    public void response_should_contain_token(String key) {
        AssertionHelper.hasJsonKey(TestContext.getLastResponse(), key);
    }
}
