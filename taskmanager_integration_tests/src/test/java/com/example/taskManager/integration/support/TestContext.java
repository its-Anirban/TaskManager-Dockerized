package com.example.taskManager.integration.support;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class TestContext {

    private static final Map<String, Object> ctx = new HashMap<>();
    private static Response lastResponse;

    public static void put(String key, Object value) {
        ctx.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) ctx.get(key);
    }

    public static void clear() {
        ctx.clear();
        lastResponse = null;
    }

    public static void setLastResponse(Response response) {
        lastResponse = response;
    }

    public static Response getLastResponse() {
        return lastResponse;
    }
}
