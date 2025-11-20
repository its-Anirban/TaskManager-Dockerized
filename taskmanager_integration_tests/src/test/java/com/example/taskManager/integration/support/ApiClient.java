package com.example.taskManager.integration.support;

import io.restassured.response.Response;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import io.qameta.allure.restassured.AllureRestAssured;

public class ApiClient {

    private static RequestSpecification baseRequest() {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON);
    }

    public static Response post(String path, Object body, String token) {
        RequestSpecification req = baseRequest().body(body);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return req.when().post(path);
    }

    public static Response get(String path, String token) {
        RequestSpecification req = baseRequest();
        if (token != null) req.header("Authorization", "Bearer " + token);
        return req.when().get(path);
    }

    public static Response put(String path, Object body, String token) {
        RequestSpecification req = baseRequest().body(body);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return req.when().put(path);
    }

    public static Response delete(String path, String token) {
        RequestSpecification req = baseRequest();
        if (token != null) req.header("Authorization", "Bearer " + token);
        return req.when().delete(path);
    }
}
