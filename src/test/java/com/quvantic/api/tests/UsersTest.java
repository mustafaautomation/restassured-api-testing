package com.quvantic.api.tests;

import com.quvantic.api.models.CreateUserRequest;
import com.quvantic.api.models.User;
import com.quvantic.api.specs.ResponseSpecs;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("User Management")
@Feature("Users API")
class UsersTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Story("List users")
    @DisplayName("GET /api/users returns paginated list")
    void listUsersReturnsPaginatedResponse() {
        given()
            .queryParam("page", 1)
        .when()
            .get("/api/users")
        .then()
            .spec(ResponseSpecs.successSpec())
            .body("data", hasSize(6))
            .body("page", equalTo(1))
            .body("total", greaterThan(0))
            .body("data[0].email", containsString("@"));
    }

    @Test
    @Tag("smoke")
    @Story("Get single user")
    @DisplayName("GET /api/users/2 returns user details")
    void getSingleUserReturnsDetails() {
        User user =
            given()
            .when()
                .get("/api/users/2")
            .then()
                .spec(ResponseSpecs.successSpec())
                .body("data.id", equalTo(2))
            .extract()
                .jsonPath().getObject("data", User.class);

        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getEmail()).contains("@");
        assertThat(user.getFirstName()).isNotBlank();
    }

    @Test
    @Tag("smoke")
    @Story("User not found")
    @DisplayName("GET /api/users/999 returns 404")
    void getNonExistentUserReturns404() {
        given()
        .when()
            .get("/api/users/999")
        .then()
            .spec(ResponseSpecs.notFoundSpec());
    }

    @Test
    @Tag("regression")
    @Story("Create user")
    @DisplayName("POST /api/users creates a new user")
    void createUserReturnsCreatedResponse() {
        CreateUserRequest request = new CreateUserRequest("Test User", "QA Engineer");

        given()
            .body(request)
        .when()
            .post("/api/users")
        .then()
            .spec(ResponseSpecs.createdSpec())
            .body("name", equalTo("Test User"))
            .body("job", equalTo("QA Engineer"))
            .body("id", notNullValue())
            .body("createdAt", notNullValue());
    }

    @Test
    @Tag("regression")
    @Story("Update user")
    @DisplayName("PUT /api/users/2 updates user")
    void updateUserReturnsUpdatedData() {
        CreateUserRequest request = new CreateUserRequest("Updated User", "Senior QA");

        given()
            .body(request)
        .when()
            .put("/api/users/2")
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated User"))
            .body("job", equalTo("Senior QA"))
            .body("updatedAt", notNullValue());
    }

    @Test
    @Tag("regression")
    @Story("Delete user")
    @DisplayName("DELETE /api/users/2 returns 204")
    void deleteUserReturns204() {
        given()
        .when()
            .delete("/api/users/2")
        .then()
            .statusCode(204);
    }

    @Test
    @Tag("regression")
    @Story("List users pagination")
    @DisplayName("GET /api/users?page=2 returns second page")
    void listUsersSecondPageReturnsData() {
        given()
            .queryParam("page", 2)
        .when()
            .get("/api/users")
        .then()
            .spec(ResponseSpecs.successSpec())
            .body("page", equalTo(2))
            .body("data", hasSize(6));
    }
}
