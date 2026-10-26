package com.quvantic.api.specs;

import com.quvantic.api.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecs {

    private RequestSpecs() {}

    public static RequestSpecification baseSpec(String baseUri) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured());
        ApiConfig.getApiKey().ifPresent(key -> builder.addHeader("x-api-key", key));
        return builder.build();
    }

    public static RequestSpecification authSpec(String baseUri, String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(baseSpec(baseUri))
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
}
