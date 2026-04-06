package com.quvantic.api.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API Quality")
@Feature("Response Validation")
class ResponseValidationTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @Tag("smoke")
    @Story("User structure")
    @DisplayName("GET /api/users/{id} returns valid user structure")
    void getUserReturnsValidStructure(int userId) {
        given()
        .when()
            .get("/api/users/" + userId)
        .then()
            .statusCode(200)
            .body("data.id", equalTo(userId))
            .body("data.email", matchesPattern("^[^@]+@[^@]+\\.[^@]+$"))
            .body("data.first_name", not(emptyOrNullString()))
            .body("data.last_name", not(emptyOrNullString()))
            .body("data.avatar", startsWith("https://"));
    }

    @Test
    @Tag("regression")
    @Story("Response headers")
    @DisplayName("API returns JSON content type")
    void apiReturnsJsonContentType() {
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));
    }

    @Test
    @Tag("regression")
    @Story("Pagination metadata")
    @DisplayName("List response has correct pagination fields")
    void listResponseHasPaginationFields() {
        given()
            .queryParam("page", 1)
            .queryParam("per_page", 3)
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("page", equalTo(1))
            .body("per_page", equalTo(3))
            .body("total", greaterThan(0))
            .body("total_pages", greaterThan(0))
            .body("data", hasSize(3));
    }

    @Test
    @Tag("regression")
    @Story("Response time")
    @DisplayName("GET /api/users responds within 2 seconds")
    void listUsersRespondsQuickly() {
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .time(lessThan(2000L));
    }

    @Test
    @Tag("regression")
    @Story("Response time")
    @DisplayName("GET /api/users/{id} responds within 2 seconds")
    void singleUserRespondsQuickly() {
        given()
        .when()
            .get("/api/users/1")
        .then()
            .statusCode(200)
            .time(lessThan(2000L));
    }

    @Test
    @Tag("regression")
    @Story("Support data")
    @DisplayName("Response includes support object")
    void responseIncludesSupport() {
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("support.url", not(emptyOrNullString()))
            .body("support.text", not(emptyOrNullString()));
    }

    @Test
    @Tag("regression")
    @Story("PATCH user")
    @DisplayName("PATCH /api/users/2 partially updates user")
    void patchUserReturnsUpdatedFields() {
        given()
            .body("{\"job\": \"AI Engineer\"}")
        .when()
            .patch("/api/users/2")
        .then()
            .statusCode(200)
            .body("job", equalTo("AI Engineer"))
            .body("updatedAt", notNullValue());
    }

    @Test
    @Tag("regression")
    @Story("Delayed response")
    @DisplayName("GET /api/users?delay=1 handles server delay")
    void delayedResponseStillReturnsData() {
        given()
            .queryParam("delay", 1)
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("data", not(empty()));
    }
}
