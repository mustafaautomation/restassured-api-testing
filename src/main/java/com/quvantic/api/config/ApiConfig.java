package com.quvantic.api.config;

import java.util.Optional;

/**
 * Run settings, read from system properties.
 *
 * <p>Without {@code -Dbase.url} the tests start a local WireMock simulation of the reqres.in API
 * and point at it. With {@code -Dbase.url=https://reqres.in} they run against the live service;
 * an optional {@code -Dapi.key=...} is sent as the {@code x-api-key} header.
 */
public final class ApiConfig {

    private ApiConfig() {}

    public static Optional<String> getConfiguredBaseUrl() {
        return Optional.ofNullable(System.getProperty("base.url")).filter(s -> !s.isBlank());
    }

    public static boolean isLive() {
        return getConfiguredBaseUrl().isPresent();
    }

    public static Optional<String> getApiKey() {
        return Optional.ofNullable(System.getProperty("api.key")).filter(s -> !s.isBlank());
    }

    public static int getDefaultTimeout() {
        return Integer.parseInt(System.getProperty("timeout", "10000"));
    }
}
