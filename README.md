# REST Assured API Testing

[![API Tests](https://github.com/mustafaautomation/restassured-api-testing/actions/workflows/api-tests.yml/badge.svg)](https://github.com/mustafaautomation/restassured-api-testing/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-ED8B00.svg?logo=openjdk&logoColor=white)](https://openjdk.org)
[![REST Assured](https://img.shields.io/badge/REST_Assured-5.5-43B02A.svg)](https://rest-assured.io)

Enterprise API testing framework built with REST Assured, JUnit 5, and Allure reporting. Demonstrates request/response specs, POJO serialization, JSON schema validation, and structured test architecture.

---

## Test Coverage

| Suite | Tests | Description |
|-------|-------|-------------|
| Users | 7 | CRUD operations, pagination, 404 handling |
| Auth | 4 | Login, registration, validation errors |
| Schema | 1 | JSON schema validation |
| **Total** | **12** | |

---

## Quick Start

```bash
git clone https://github.com/mustafaautomation/restassured-api-testing.git
cd restassured-api-testing

# Run all tests
mvn clean test

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
RequestSpecs.baseSpec();           // Base URL + JSON + Allure filter
RequestSpecs.authSpec(token);      // + Authorization header
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
│   │   │   └── ApiConfig.java          # Environment config
│   │   ├── models/
│   │   │   ├── User.java               # User POJO
│   │   │   └── CreateUserRequest.java   # Request body POJO
│   │   └── specs/
│   │       ├── RequestSpecs.java        # Reusable request specs
│   │       └── ResponseSpecs.java       # Reusable response specs
│   └── test/
│       ├── java/com/quvantic/api/tests/
│       │   ├── BaseTest.java            # Common setup
│       │   ├── UsersTest.java           # 7 user CRUD tests
│       │   ├── AuthTest.java            # 4 auth tests
│       │   └── SchemaTest.java          # JSON schema validation
│       └── resources/schemas/
│           └── user-list.json           # JSON schema for user list
├── pom.xml
└── .github/workflows/api-tests.yml
```

---

## Stack

| Tool | Purpose |
|------|---------|
| REST Assured 5.5 | HTTP client with fluent assertions |
| JUnit 5 | Test runner with tags and display names |
| Allure 2.29 | Rich HTML reporting |
| Jackson | JSON serialization/deserialization |
| AssertJ | Fluent Java assertions |
| Maven | Build and dependency management |

---

## License

MIT

---

Built by [Quvantic](https://quvantic.com)
