package com.quvantic.api.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Authentication")
@Feature("Auth API")
class AuthTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Story("Successful login")
    @DisplayName("POST /api/login returns token")
    void loginReturnsToken() {
        given()
            .body(Map.of("email", "eve.holt@reqres.in", "password", "cityslicka"))
        .when()
            .post("/api/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", not(emptyString()));
    }

    @Test
    @Tag("smoke")
    @Story("Login validation")
    @DisplayName("POST /api/login fails without password")
    void loginFailsWithoutPassword() {
        given()
            .body(Map.of("email", "eve.holt@reqres.in"))
        .when()
            .post("/api/login")
        .then()
            .statusCode(400)
            .body("error", equalTo("Missing password"));
    }

    @Test
    @Tag("regression")
    @Story("Registration")
    @DisplayName("POST /api/register creates account")
    void registerReturnsIdAndToken() {
        given()
            .body(Map.of("email", "eve.holt@reqres.in", "password", "pistol"))
        .when()
            .post("/api/register")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("token", notNullValue());
    }

    @Test
    @Tag("regression")
    @Story("Registration validation")
    @DisplayName("POST /api/register fails without password")
    void registerFailsWithoutPassword() {
        given()
            .body(Map.of("email", "eve.holt@reqres.in"))
        .when()
            .post("/api/register")
        .then()
            .statusCode(400)
            .body("error", equalTo("Missing password"));
    }
}
