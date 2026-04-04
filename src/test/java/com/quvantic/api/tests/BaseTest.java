package com.quvantic.api.tests;

import com.quvantic.api.specs.RequestSpecs;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    @BeforeAll
    static void setupAll() {
        RestAssured.requestSpecification = RequestSpecs.baseSpec();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
