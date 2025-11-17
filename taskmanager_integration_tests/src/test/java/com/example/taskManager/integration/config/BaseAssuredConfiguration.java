package com.example.taskManager.integration.config;


import io.restassured.RestAssured;
import org.junit.BeforeClass;

public class BaseAssuredConfiguration {

    @BeforeClass
    public static void setup() {
        String base = System.getProperty("base.url", "http://localhost:8080");
        RestAssured.baseURI = base;
        RestAssured.useRelaxedHTTPSValidation();
    }
}
