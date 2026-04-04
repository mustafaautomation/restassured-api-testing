package com.quvantic.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.lessThan;

public final class ResponseSpecs {

    private ResponseSpecs() {}

    public static ResponseSpecification successSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectResponseTime(lessThan(5000L))
                .build();
    }

    public static ResponseSpecification createdSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectResponseTime(lessThan(5000L))
                .build();
    }

    public static ResponseSpecification notFoundSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }
}
