package com.example.taskManager.integration.stepdefinitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.taskManager.integration.config.TestConfiguration;
import com.example.taskManager.integration.support.ApiClient;
import com.example.taskManager.integration.support.JsonHelper;
import com.example.taskManager.integration.support.TestContext;
import com.example.taskManager.integration.support.TokenStorage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

public class TaskStepDefinitions {

    @Given("I have a task with title {string}")
    public void i_have_a_task_with_title(String title) {

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", "");
        body.put("completed", false);

        Response r = ApiClient.post(
                TestConfiguration.TASKS,
                JsonHelper.toJson(body),
                TokenStorage.getToken()
        );

        TestContext.setLastResponse(r);
        assertEquals(201, r.getStatusCode(), "Task creation must return 201");

        Long id = r.jsonPath().getLong("id");
        assertNotNull(id, "Task ID must not be null");
        TestContext.put("taskId", id);
    }

    @Given("I create a task with title {string} and description {string}")
    public void i_create_a_task(String title, String description) {

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("completed", false);

        Response r = ApiClient.post(TestConfiguration.TASKS, JsonHelper.toJson(body), TokenStorage.getToken());
        TestContext.setLastResponse(r);

        assertEquals(201, r.getStatusCode(), "Expected HTTP 201 Created");

        Long id = r.jsonPath().getLong("id");
        assertNotNull(id, "Task ID must not be null");

        TestContext.put("taskId", id);
    }

    @When("I fetch that task")
    public void i_fetch_that_task() {
        Long id = TestContext.get("taskId");
        Response r = ApiClient.get(TestConfiguration.TASKS + "/" + id, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I fetch all tasks")
    public void i_fetch_all_tasks() {
        Response r = ApiClient.get(TestConfiguration.TASKS, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I request all tasks")
    public void i_request_all_tasks() {
        Response r = ApiClient.get(TestConfiguration.TASKS, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I update that task with new title {string} and description {string}")
    public void i_update_that_task(String title, String description) {

        Long id = TestContext.get("taskId");

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("completed", false);

        Response r = ApiClient.put(TestConfiguration.TASKS + "/" + id, JsonHelper.toJson(body), TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I update that task to set completed true")
    public void i_update_that_task_to_set_completed_true() {

        Long id = TestContext.get("taskId");

        // ✔ Backend requires full JSON body → prevent 500 errors
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Write tests");     // fallback title
        body.put("description", "");          // allowed empty
        body.put("completed", true);

        Response r = ApiClient.put(TestConfiguration.TASKS + "/" + id, JsonHelper.toJson(body), TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @When("I delete that task")
    public void i_delete_that_task() {
        Long id = TestContext.get("taskId");
        Response r = ApiClient.delete(TestConfiguration.TASKS + "/" + id, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }

    @Then("the response should contain a numeric {string}")
    public void the_response_should_contain_a_numeric(String key) {
        Object v = TestContext.getLastResponse().jsonPath().get(key);
        assertTrue(v instanceof Number, "Expected numeric: " + key);
    }

    @Then("the response should contain {string}: true")
    public void the_response_should_contain_true(String key) {
        Boolean v = TestContext.getLastResponse().jsonPath().getBoolean(key);
        assertNotNull(v, "Key missing");
        assertTrue(v, "Expected true for: " + key);
    }

    @Then("the response should be a list")
    public void the_response_should_be_a_list() {
        List<?> list = TestContext.getLastResponse().jsonPath().getList("$");
        assertNotNull(list);
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(Integer expected) {
        assertEquals(expected.intValue(), TestContext.getLastResponse().getStatusCode());
    }
}
