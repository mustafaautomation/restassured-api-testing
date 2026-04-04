package com.quvantic.api.config;

public final class ApiConfig {

    private ApiConfig() {}

    public static String getBaseUrl() {
        return System.getProperty("base.url", "https://reqres.in");
    }

    public static int getDefaultTimeout() {
        return Integer.parseInt(System.getProperty("timeout", "10000"));
    }
}
