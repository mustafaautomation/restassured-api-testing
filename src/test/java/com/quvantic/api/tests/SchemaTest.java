package com.quvantic.api.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Epic("Schema Validation")
@Feature("JSON Schema")
class SchemaTest extends BaseTest {

    @Test
    @Tag("regression")
    @Story("User list schema")
    @DisplayName("GET /api/users response matches JSON schema")
    void userListMatchesSchema() {
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/user-list.json"));
    }
}
