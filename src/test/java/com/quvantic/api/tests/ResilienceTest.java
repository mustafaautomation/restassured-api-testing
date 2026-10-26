package com.quvantic.api.tests;

import com.quvantic.api.models.CreateUserRequest;
import com.quvantic.api.retry.RateLimitRetry;
import com.quvantic.api.specs.ResponseSpecs;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.equalTo;

@Epic("API Quality")
@Feature("Faults and request verification")
class ResilienceTest extends SimulationOnlyTest {

    private final List<Duration> waits = new ArrayList<>();
    private final RateLimitRetry retry =
            new RateLimitRetry(3, Duration.ofMillis(50), Duration.ofSeconds(2), waits::add);

    @Test
    @Story("Rate limiting")
    @DisplayName("429 with Retry-After is retried and the second answer is used")
    void retriesAfterRateLimit() {
        wireMock.stubFor(get(urlPathEqualTo("/api/users/2")).atPriority(1).inScenario("rate-limit")
                .whenScenarioStateIs(STARTED).willSetStateTo("open")
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "1")));

        Response response = retry.call(() -> given().get("/api/users/2"));

        response.then().statusCode(200).body("data.id", equalTo(2));
        assertThat(waits).containsExactly(Duration.ofSeconds(1));
        wireMock.verify(2, getRequestedFor(urlPathEqualTo("/api/users/2")));
    }

    @Test
    @Story("Rate limiting")
    @DisplayName("Persistent 503 gives up after the attempt limit and returns the 503")
    void givesUpAfterMaxAttempts() {
        wireMock.stubFor(get(urlPathEqualTo("/api/users")).atPriority(1)
                .willReturn(aResponse().withStatus(503)));

        Response response = retry.call(() -> given().get("/api/users"));

        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(waits).containsExactly(Duration.ofMillis(50), Duration.ofMillis(100));
        wireMock.verify(3, getRequestedFor(urlPathEqualTo("/api/users")));
    }

    @Test
    @Story("Rate limiting")
    @DisplayName("Client errors such as 404 are not retried")
    void doesNotRetryClientErrors() {
        Response response = retry.call(() -> given().get("/api/users/999"));

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(waits).isEmpty();
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/api/users/999")));
    }

    @Test
    @Story("Response time")
    @DisplayName("successSpec fails a response slower than its budget")
    void slowResponseFailsTheTimeBudget() {
        wireMock.stubFor(get(urlPathEqualTo("/api/users/3")).atPriority(1)
                .willReturn(aResponse().withStatus(200).withFixedDelay(400).withBody("{}")));

        assertThatThrownBy(() -> given().get("/api/users/3").then().spec(ResponseSpecs.successSpec(200)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("response time");
    }

    @Test
    @Story("Schema drift")
    @DisplayName("Schema check catches a user without an avatar")
    void schemaCatchesMissingField() {
        wireMock.stubFor(get(urlPathEqualTo("/api/users")).atPriority(1).willReturn(okJson(
                "{\"page\":1,\"per_page\":6,\"total\":1,\"total_pages\":1,"
                        + "\"data\":[{\"id\":1,\"email\":\"a@b.co\",\"first_name\":\"A\",\"last_name\":\"B\"}]}")));

        assertThatThrownBy(() -> given().get("/api/users").then()
                .body(matchesJsonSchemaInClasspath("schemas/user-list.json")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("avatar");
    }

    @Test
    @Story("Schema drift")
    @DisplayName("Schema check catches a type change (id as string)")
    void schemaCatchesTypeChange() {
        wireMock.stubFor(get(urlPathEqualTo("/api/users")).atPriority(1).willReturn(okJson(
                "{\"page\":1,\"per_page\":6,\"total\":1,\"total_pages\":1,\"data\":[{\"id\":\"1\","
                        + "\"email\":\"a@b.co\",\"first_name\":\"A\",\"last_name\":\"B\",\"avatar\":\"https://x.test/a.jpg\"}]}")));

        assertThatThrownBy(() -> given().get("/api/users").then()
                .body(matchesJsonSchemaInClasspath("schemas/user-list.json")))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @Story("Request verification")
    @DisplayName("Create user sends a JSON body with exactly the model fields")
    void createUserSendsExpectedRequest() {
        given().body(new CreateUserRequest("Test User", "QA Engineer")).post("/api/users").then().statusCode(201);

        wireMock.verify(postRequestedFor(urlPathEqualTo("/api/users"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{\"name\":\"Test User\",\"job\":\"QA Engineer\"}")));
    }

    @Test
    @Story("Request verification")
    @DisplayName("No API key header is sent unless one is configured")
    void noApiKeyByDefault() {
        given().get("/api/users/1").then().statusCode(200);

        wireMock.verify(getRequestedFor(urlPathEqualTo("/api/users/1")).withoutHeader("x-api-key"));
    }
}
