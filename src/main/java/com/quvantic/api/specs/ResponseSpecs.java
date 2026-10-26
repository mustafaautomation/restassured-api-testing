package com.quvantic.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.lessThan;

public final class ResponseSpecs {

    /** Default response-time budget for the success specs. */
    public static final long DEFAULT_MAX_MILLIS = 5000L;

    private ResponseSpecs() {}

    public static ResponseSpecification successSpec() {
        return successSpec(DEFAULT_MAX_MILLIS);
    }

    public static ResponseSpecification successSpec(long maxMillis) {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectResponseTime(lessThan(maxMillis))
                .build();
    }

    public static ResponseSpecification createdSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectResponseTime(lessThan(DEFAULT_MAX_MILLIS))
                .build();
    }

    public static ResponseSpecification notFoundSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }
}
