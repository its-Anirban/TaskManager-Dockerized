package com.example.taskManager.integration.stepdefinitions;

import java.util.Map;

import com.example.taskManager.integration.config.TestConfiguration;
import com.example.taskManager.integration.support.ApiClient;
import com.example.taskManager.integration.support.JsonHelper;
import com.example.taskManager.integration.support.TestContext;
import com.example.taskManager.integration.support.TokenStorage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class TaskStepDefinitions {

    @Given("I am logged in as {string} with password {string}")
    public void logged_in(String username, String password) {
        Map<String, String> body = Map.of("username", username, "password", password);
        Response r = ApiClient.post(TestConfiguration.AUTH_LOGIN, JsonHelper.toJson(body), null);
        TestContext.setLastResponse(r);

        if (r.getStatusCode() == 200) {
            String tok = r.getBody().jsonPath().getString("token");
            TokenStorage.setToken(tok);
        }
    }

    @When("I create a task with title {string} and description {string}")
    public void create_task(String title, String desc) {
        Map<String, Object> body = Map.of("title", title, "description", desc, "completed", false);
        Response r = ApiClient.post(TestConfiguration.TASKS, JsonHelper.toJson(body), TokenStorage.getToken());
        TestContext.setLastResponse(r);

        if (r.getStatusCode() == 201) {
            Long id = r.getBody().jsonPath().getLong("id");
            TestContext.put("lastTaskId", id);
        }
    }

    @When("I request all tasks")
    public void get_tasks() {
        Response r = ApiClient.get(TestConfiguration.TASKS, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @Then("the response should be a list")
    public void response_should_be_list() {
        Object list = TestContext.getLastResponse().getBody().jsonPath().getList("$");
        if (list == null)
            throw new AssertionError("Expected list");
    }

    @Given("I have a task with title {string}")
    public void ensure_task(String title) {
        create_task(title, "auto-created");
    }

    @When("I update that task to set completed true")
    public void update_task() {
        Long id = TestContext.get("lastTaskId", Long.class);
        Map<String, Object> body = Map.of("title", "Updated", "description", "Updated", "completed", true);
        Response r = ApiClient.put(TestConfiguration.TASKS + "/" + id, JsonHelper.toJson(body),
                TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I delete that task")
    public void delete_task() {
        Long id = TestContext.get("lastTaskId", Long.class);
        Response r = ApiClient.delete(TestConfiguration.TASKS + "/" + id, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @Then("the response should contain a numeric {string}")
    public void response_contains_numeric(String key) {
        Object v = TestContext.getLastResponse().getBody().jsonPath().get(key);
        if (!(v instanceof Number)) {
            throw new AssertionError("Expected numeric key: " + key);
        }
    }

    @Then("the response should contain {string}: true")
    public void response_should_contain_true(String key) {
        Boolean value = TestContext.getLastResponse().getBody().jsonPath().getBoolean(key);
        if (value == null || !value) {
            throw new AssertionError("Expected " + key + " to be true");
        }
    }

}
