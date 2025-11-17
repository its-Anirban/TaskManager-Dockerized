package com.example.taskManager.integration.support;

import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AssertionHelper {

    public static void status(Response r, int code) {
        assertThat(r.getStatusCode(), is(code));
    }

    public static void hasJsonKey(Response r, String key) {
        assertThat(r.getBody().jsonPath().get(key), notNullValue());
    }

    public static void jsonValueEquals(Response r, String path, Object value) {
        assertThat(r.getBody().jsonPath().get(path), equalTo(value));
    }
}
