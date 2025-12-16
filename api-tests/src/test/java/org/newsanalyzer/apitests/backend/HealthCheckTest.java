package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.config.Endpoints;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Health check tests for the Java Spring Boot backend service.
 * These are smoke tests to verify the backend is running and healthy.
 */
@Tag("backend")
@Tag("smoke")
@DisplayName("Backend Health Check Tests")
class HealthCheckTest extends BaseApiTest {

    @Test
    @DisplayName("GET /actuator/health returns 200 OK")
    void shouldReturnHealthyStatus_whenActuatorHealthCalled() {
        given()
            .spec(getBackendSpec())
        .when()
            .get(Endpoints.Backend.HEALTH)
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health response contains status UP")
    void shouldContainStatusUp_whenHealthy() {
        given()
            .spec(getBackendSpec())
        .when()
            .get(Endpoints.Backend.HEALTH)
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
