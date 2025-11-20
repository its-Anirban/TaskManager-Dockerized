package com.example.taskManager.integration.support;

public class TokenStorage {

    private static String token;

    public static void setToken(String t) {
        token = t;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {
        token = null;
    }
}
