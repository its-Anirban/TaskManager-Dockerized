package com.example.taskManager.integration.stepdefinitions;

import com.example.taskManager.integration.support.ApiClient;
import com.example.taskManager.integration.support.TestContext;
import com.example.taskManager.integration.support.TokenStorage;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class CommonStepDefinitions {

    @When("I call GET {string}")
    public void call_get(String path) {
        Response r = ApiClient.get(path, TokenStorage.getToken());
        TestContext.setLastResponse(r);
    }
}
