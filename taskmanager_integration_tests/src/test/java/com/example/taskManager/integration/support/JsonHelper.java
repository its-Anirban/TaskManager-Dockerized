package com.example.taskManager.integration.support;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {
    private static final ObjectMapper M = new ObjectMapper();

    public static String toJson(Object o) {
        try { return M.writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
