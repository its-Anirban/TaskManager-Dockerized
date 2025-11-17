package com.example.taskManager.integration.support;

import io.restassured.response.Response;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class ApiClient {

    public static Response post(String path, Object body, String token) {
        if (token == null) {
            return given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post(path);
        } else {
            return given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(body)
                    .when().post(path);
        }
    }

    public static Response get(String path, String token) {
        if (token == null) {
            return given().when().get(path);
        } else {
            return given().header("Authorization", "Bearer " + token).when().get(path);
        }
    }

    public static Response put(String path, Object body, String token) {
        if (token == null) {
            return given().contentType(ContentType.JSON).body(body).when().put(path);
        } else {
            return given().contentType(ContentType.JSON).header("Authorization", "Bearer " + token).body(body).when().put(path);
        }
    }

    public static Response delete(String path, String token) {
        if (token == null) {
            return given().when().delete(path);
        } else {
            return given().header("Authorization", "Bearer " + token).when().delete(path);
        }
    }
}
