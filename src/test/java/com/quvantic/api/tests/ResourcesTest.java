package com.quvantic.api.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Resources")
@Feature("Unknown Resources API")
class ResourcesTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Story("List resources")
    @DisplayName("GET /api/unknown returns resource list")
    void listResourcesReturnsData() {
        given()
        .when()
            .get("/api/unknown")
        .then()
            .statusCode(200)
            .body("data", not(empty()))
            .body("data[0].id", notNullValue())
            .body("data[0].name", not(emptyOrNullString()))
            .body("data[0].color", matchesPattern("^#[0-9a-fA-F]{6}$"));
    }

    @Test
    @Tag("smoke")
    @Story("Single resource")
    @DisplayName("GET /api/unknown/2 returns resource details")
    void getSingleResourceReturnsDetails() {
        given()
        .when()
            .get("/api/unknown/2")
        .then()
            .statusCode(200)
            .body("data.id", equalTo(2))
            .body("data.name", not(emptyOrNullString()))
            .body("data.year", greaterThan(0))
            .body("data.color", matchesPattern("^#[0-9a-fA-F]{6}$"))
            .body("data.pantone_value", not(emptyOrNullString()));
    }

    @Test
    @Tag("regression")
    @Story("Resource not found")
    @DisplayName("GET /api/unknown/999 returns 404")
    void getNonExistentResourceReturns404() {
        given()
        .when()
            .get("/api/unknown/999")
        .then()
            .statusCode(404);
    }

    @Test
    @Tag("regression")
    @Story("Resource pagination")
    @DisplayName("GET /api/unknown?page=2 returns second page")
    void listResourcesSecondPage() {
        given()
            .queryParam("page", 2)
        .when()
            .get("/api/unknown")
        .then()
            .statusCode(200)
            .body("page", equalTo(2))
            .body("data", not(empty()));
    }
}
