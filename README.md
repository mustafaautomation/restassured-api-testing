# REST Assured API Testing

[![API Tests](https://github.com/mustafaautomation/restassured-api-testing/actions/workflows/api-tests.yml/badge.svg)](https://github.com/mustafaautomation/restassured-api-testing/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20%7C%2025-ED8B00.svg?logo=openjdk&logoColor=white)](https://openjdk.org)
[![REST Assured](https://img.shields.io/badge/REST_Assured-6.3-43B02A.svg)](https://rest-assured.io)

API testing framework (portfolio project) built with REST Assured, JUnit and Allure reporting. The tests are written against the reqres.in demo API. By default they run against a local [WireMock](https://wiremock.org) simulation of that API, so CI is offline and deterministic. Pass `-Dbase.url` to run the same tests against the live service.

It demonstrates request/response specs, POJO serialization, JSON schema validation, a retry helper for rate limits, and fault-injection tests (429/503, slow responses, schema drift) that a public API can't provide on demand.

### Why a simulation

The suite used to call reqres.in directly. That service rate-limits traffic, and a run in October 2026 got HTTP 429 on all 28 tests, failures that had nothing to do with the code. The WireMock server in `src/test/java/.../support/ReqresSimulation.java` answers the endpoints these tests use with reqres.in's demo data. It is a simulation, so passing tests prove the framework and the assertions work, not that reqres.in is up. Live mode exists for that.

---

## Test Coverage

| Suite | Tests | Description |
|-------|-------|-------------|
| Users | 7 | CRUD operations, pagination, 404 handling |
| Auth | 4 | Login, registration, validation errors |
| Schema | 1 | JSON schema validation |
| Resources | 4 | Resource list and single-resource endpoints |
| Response validation | 12 | Response structure (parameterized), headers, PATCH/PUT fields |
| Resilience (simulation only) | 8 | 429/503 retry, response-time budget, schema drift, request verification |
| RateLimitRetry unit tests | 5 | Backoff, `Retry-After` handling, wait cap |
| **Total** | **41** | |

The 8 resilience tests need control over the server, so they are skipped in live mode.

---

## Quick Start

```bash
git clone https://github.com/mustafaautomation/restassured-api-testing.git
cd restassured-api-testing

# Run all tests against the local WireMock simulation (JDK 21+)
mvn clean test

# Run against the live API (optional api.key is sent as the x-api-key header)
mvn test -Dbase.url=https://reqres.in -Dapi.key=YOUR_KEY

# Run smoke tests only
mvn test -Dgroups=smoke

# Run regression tests
mvn test -Dgroups=regression

# Generate Allure report
mvn allure:serve
```

---

## Architecture

```
┌──────────────────────────────────────────┐
│            Test Classes                   │
│  UsersTest │ AuthTest │ SchemaTest        │
├──────────────────────────────────────────┤
│            Request/Response Specs         │
│  RequestSpecs.baseSpec() / authSpec()     │
│  ResponseSpecs.successSpec() / 201 / 404  │
├──────────────────────────────────────────┤
│            Models (POJOs)                 │
│  User │ CreateUserRequest                 │
├──────────────────────────────────────────┤
│            Config                         │
│  ApiConfig (base URL, timeout)            │
├──────────────────────────────────────────┤
│            REST Assured + Allure          │
└──────────────────────────────────────────┘
```

---

## Key Patterns

### Request Specifications
Reusable request configs with base URI, content type, and Allure logging:

```java
RequestSpecs.baseSpec(baseUrl);         // Base URL + JSON + Allure filter (+ x-api-key if configured)
RequestSpecs.authSpec(baseUrl, token);  // + Authorization header
```

### Response Specifications
Reusable response assertions:

```java
ResponseSpecs.successSpec();       // 200 + response time < 5s
ResponseSpecs.createdSpec();       // 201 + response time < 5s
ResponseSpecs.notFoundSpec();      // 404
```

### POJO Serialization
Jackson-based request/response mapping:

```java
User user = given()
    .get("/api/users/2")
    .jsonPath().getObject("data", User.class);
```

### Retrying rate-limited calls
`RateLimitRetry` retries 429 and 503 responses, honouring a numeric `Retry-After` header (capped) and otherwise backing off exponentially. After the attempt limit it returns the last response, so the failure still shows in the test:

```java
Response r = RateLimitRetry.defaults().call(() -> given().get("/api/users/2"));
```

### JSON Schema Validation
Validate response structure against JSON schemas:

```java
.body(matchesJsonSchemaInClasspath("schemas/user-list.json"));
```

---

## Project Structure

```
restassured-api-testing/
├── src/
│   ├── main/java/com/quvantic/api/
│   │   ├── config/
│   │   │   └── ApiConfig.java          # base.url / api.key settings
│   │   ├── retry/
│   │   │   └── RateLimitRetry.java     # 429/503 retry helper
│   │   ├── models/
│   │   │   ├── User.java               # User POJO
│   │   │   └── CreateUserRequest.java   # Request body POJO
│   │   └── specs/
│   │       ├── RequestSpecs.java        # Reusable request specs
│   │       └── ResponseSpecs.java       # Reusable response specs
│   └── test/
│       ├── java/com/quvantic/api/tests/
│       │   ├── support/ReqresSimulation.java # WireMock simulation of reqres.in
│       │   ├── BaseTest.java            # Live or simulation setup
│       │   ├── ResilienceTest.java      # 8 fault-injection / verification tests
│       │   ├── UsersTest.java           # 7 user CRUD tests
│       │   ├── AuthTest.java            # 4 auth tests
│       │   ├── SchemaTest.java          # JSON schema validation
│       │   ├── ResourcesTest.java       # 4 resource tests
│       │   └── ResponseValidationTest.java # 12 response checks
│       └── resources/schemas/
│           └── user-list.json           # JSON schema for user list
├── pom.xml
└── .github/workflows/api-tests.yml
```

---

## CI

`.github/workflows/api-tests.yml` runs `mvn -B test` on JDK 21 and 25 against the simulation for every push and pull request. A manual run (workflow_dispatch with `live: true`) also runs the suite against reqres.in using the `REQRES_API_KEY` repository secret.

---

## Stack

| Tool | Purpose |
|------|---------|
| Java 21 (CI also runs JDK 25) | Language level |
| REST Assured 6.3 | HTTP client with fluent assertions |
| JUnit 6 | Test runner with tags and display names |
| WireMock 3.13 | Local simulation of the API, fault injection, request verification |
| Allure 2.35 | HTML reporting |
| Jackson | JSON serialization/deserialization |
| AssertJ | Fluent Java assertions |
| Maven | Build and dependency management |

---

## License

MIT

---

Built by [Quvantic](https://quvantic.com)
