package com.example.taskManager.integration.config;

import org.junit.BeforeClass;
import io.restassured.RestAssured;

public class RestAssuredConfig {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = TestConfiguration.BASE_URL;
    }
}
