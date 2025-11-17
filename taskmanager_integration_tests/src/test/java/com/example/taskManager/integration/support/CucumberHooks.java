package com.example.taskManager.integration.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;

public class CucumberHooks {

    @Before
    public void beforeScenario() {
        // clear token between scenarios by default
        TokenStorage.clear();
    }

    @After
    public void afterScenario() {
        // optional cleanup
    }
}
