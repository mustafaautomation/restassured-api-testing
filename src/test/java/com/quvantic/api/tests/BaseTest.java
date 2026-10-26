package com.quvantic.api.tests;

import com.quvantic.api.config.ApiConfig;
import com.quvantic.api.specs.RequestSpecs;
import com.quvantic.api.support.ReqresSimulation;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

/**
 * Points REST Assured at the live API when {@code -Dbase.url} is set, otherwise at the local
 * WireMock simulation.
 */
public abstract class BaseTest {

    protected static String baseUrl;

    @BeforeAll
    static void setupAll() {
        baseUrl = ApiConfig.getConfiguredBaseUrl().orElseGet(ReqresSimulation::baseUrl);
        RestAssured.requestSpecification = RequestSpecs.baseSpec(baseUrl);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
