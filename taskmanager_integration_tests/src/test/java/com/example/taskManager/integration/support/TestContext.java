package com.example.taskManager.integration.support;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class TestContext {

    private static final Map<String, Object> ctx = new HashMap<>();
    private static Response lastResponse;

    public static void put(String k, Object v) { 
        ctx.put(k, v); 
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String k, Class<T> clz) { 
        return (T) ctx.get(k); 
    }

    public static void clear() { 
        ctx.clear(); 
        lastResponse = null;
    }

    public static void setLastResponse(Response r) {
        lastResponse = r;
    }

    public static Response getLastResponse() {
        return lastResponse;
    }
}
