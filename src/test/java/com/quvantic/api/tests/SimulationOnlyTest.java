package com.quvantic.api.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.quvantic.api.config.ApiConfig;
import com.quvantic.api.support.ReqresSimulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Base for tests that need control over the server: injected faults, delays, malformed bodies
 * and request verification. These only run against the local simulation.
 */
@Tag("simulation")
abstract class SimulationOnlyTest extends BaseTest {

    protected WireMockServer wireMock;

    @BeforeEach
    void requireSimulation() {
        assumeFalse(ApiConfig.isLive(), "needs the local WireMock simulation");
        wireMock = ReqresSimulation.server();
        ReqresSimulation.reset();
    }

    @AfterEach
    void restoreStubs() {
        if (!ApiConfig.isLive()) {
            ReqresSimulation.reset();
        }
    }
}
