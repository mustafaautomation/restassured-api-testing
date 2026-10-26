package com.quvantic.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * A local WireMock server that answers like the public reqres.in API for the endpoints these
 * tests use. The data mirrors reqres.in's fixed demo data (12 users, 12 colour resources).
 *
 * <p>It is a simulation, not the real service: it exists so the suite runs offline and gives the
 * same answer every time. Anything about reqres.in itself is only checked in live mode.
 */
public final class ReqresSimulation {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String[][] USERS = {
        {"George", "Bluth"}, {"Janet", "Weaver"}, {"Emma", "Wong"}, {"Eve", "Holt"},
        {"Charles", "Morris"}, {"Tracey", "Ramos"}, {"Michael", "Lawson"}, {"Lindsay", "Ferguson"},
        {"Tobias", "Funke"}, {"Byron", "Fields"}, {"George", "Edwards"}, {"Rachel", "Howell"},
    };
    private static final Object[][] RESOURCES = {
        {"cerulean", 2000, "#98B2D1", "15-4020"}, {"fuchsia rose", 2001, "#C74375", "17-2031"},
        {"true red", 2002, "#BF1932", "19-1664"}, {"aqua sky", 2003, "#7BC4C4", "14-4811"},
        {"tigerlily", 2004, "#E2583E", "17-1456"}, {"blue turquoise", 2005, "#53B0AE", "15-5217"},
        {"sand dollar", 2006, "#DECDBE", "13-1106"}, {"chili pepper", 2007, "#9B1B30", "19-1557"},
        {"blue iris", 2008, "#5A5B9F", "18-3943"}, {"mimosa", 2009, "#F0C05A", "14-0848"},
        {"turquoise", 2010, "#45B5AA", "15-5519"}, {"honeysuckle", 2011, "#D94F70", "18-2120"},
    };
    private static final Map<String, String> SUPPORT = Map.of(
            "url", "https://reqres.in/#support-heading",
            "text", "Simulated support block, shaped like the one reqres.in returns");

    private static WireMockServer server;

    private ReqresSimulation() {}

    /** Starts the shared server on first use and returns it. */
    public static synchronized WireMockServer server() {
        if (server == null) {
            server = new WireMockServer(options()
                    .dynamicPort()
                    .templatingEnabled(true)
                    .globalTemplating(false));
            server.start();
            registerStubs(server);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        }
        return server;
    }

    public static String baseUrl() {
        return server().baseUrl();
    }

    /** Clears per-test stubs and request history, then restores the baseline stubs. */
    public static void reset() {
        WireMockServer s = server();
        s.resetAll();
        registerStubs(s);
    }

    static Map<String, Object> user(int id) {
        String first = USERS[id - 1][0];
        String last = USERS[id - 1][1];
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("id", id);
        u.put("email", (first + "." + last).toLowerCase() + "@reqres.in");
        u.put("first_name", first);
        u.put("last_name", last);
        u.put("avatar", "https://reqres.in/img/faces/" + id + "-image.jpg");
        return u;
    }

    static Map<String, Object> resource(int id) {
        Object[] r = RESOURCES[id - 1];
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", r[0]);
        m.put("year", r[1]);
        m.put("color", r[2]);
        m.put("pantone_value", r[3]);
        return m;
    }

    private static String page(int page, int perPage, int total, java.util.function.IntFunction<Map<String, Object>> item) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int id = (page - 1) * perPage + 1; id <= Math.min(page * perPage, total); id++) {
            data.add(item.apply(id));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("page", page);
        body.put("per_page", perPage);
        body.put("total", total);
        body.put("total_pages", (total + perPage - 1) / perPage);
        body.put("data", data);
        body.put("support", SUPPORT);
        return write(body);
    }

    private static String single(Map<String, Object> data) {
        return write(Map.of("data", data, "support", SUPPORT));
    }

    private static String write(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void json(WireMockServer s, MappingBuilder mapping, int status, String body) {
        s.stubFor(mapping.willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(body)));
    }

    private static void registerStubs(WireMockServer s) {
        int users = USERS.length;
        int resources = RESOURCES.length;

        // Lists. Unmatched query params fall back to page 1 with 6 per page, as reqres.in does.
        for (int p = 1; p <= 2; p++) {
            json(s, get(urlPathEqualTo("/api/users")).withQueryParam("page", equalTo(String.valueOf(p))),
                    200, page(p, 6, users, ReqresSimulation::user));
            json(s, get(urlPathEqualTo("/api/unknown")).withQueryParam("page", equalTo(String.valueOf(p))),
                    200, page(p, 6, resources, ReqresSimulation::resource));
        }
        json(s, get(urlPathEqualTo("/api/users")).atPriority(10), 200, page(1, 6, users, ReqresSimulation::user));
        json(s, get(urlPathEqualTo("/api/unknown")).atPriority(10), 200,
                page(1, 6, resources, ReqresSimulation::resource));
        json(s, get(urlPathEqualTo("/api/users")).withQueryParam("per_page", equalTo("3"))
                .withQueryParam("page", equalTo("1")).atPriority(1), 200, page(1, 3, users, ReqresSimulation::user));
        s.stubFor(get(urlPathEqualTo("/api/users")).withQueryParam("delay", matching("\\d+")).atPriority(1)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withFixedDelay(1000)
                        .withBody(page(1, 6, users, ReqresSimulation::user))));

        // Single items, and 404 with an empty object for anything else.
        for (int id = 1; id <= users; id++) {
            json(s, get(urlPathEqualTo("/api/users/" + id)), 200, single(user(id)));
        }
        for (int id = 1; id <= resources; id++) {
            json(s, get(urlPathEqualTo("/api/unknown/" + id)), 200, single(resource(id)));
        }
        json(s, get(urlPathMatching("/api/users/\\d+")).atPriority(10), 404, "{}");
        json(s, get(urlPathMatching("/api/unknown/\\d+")).atPriority(10), 404, "{}");

        // Writes echo the request fields back, like reqres.in.
        s.stubFor(post(urlPathEqualTo("/api/users")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withTransformers("response-template")
                .withBody("{\"name\":\"{{jsonPath request.body '$.name'}}\",\"job\":\"{{jsonPath request.body '$.job'}}\","
                        + "\"id\":\"{{randomInt lower=100 upper=999}}\",\"createdAt\":\"{{now}}\"}")));
        s.stubFor(put(urlPathMatching("/api/users/\\d+")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withTransformers("response-template")
                .withBody("{\"name\":\"{{jsonPath request.body '$.name'}}\",\"job\":\"{{jsonPath request.body '$.job'}}\","
                        + "\"updatedAt\":\"{{now}}\"}")));
        s.stubFor(patch(urlPathMatching("/api/users/\\d+")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withTransformers("response-template")
                .withBody("{\"job\":\"{{jsonPath request.body '$.job'}}\",\"updatedAt\":\"{{now}}\"}")));
        s.stubFor(delete(urlPathMatching("/api/users/\\d+")).willReturn(aResponse().withStatus(204)));

        // Auth: only the demo account is known; a missing password is a 400.
        String knownEmail = "eve.holt@reqres.in";
        json(s, post(urlPathEqualTo("/api/login"))
                .withRequestBody(matchingJsonPath("$.email", equalTo(knownEmail)))
                .withRequestBody(matchingJsonPath("$.password")), 200, "{\"token\":\"QpwL5tke4Pnpja7X4\"}");
        json(s, post(urlPathEqualTo("/api/register"))
                .withRequestBody(matchingJsonPath("$.email", equalTo(knownEmail)))
                .withRequestBody(matchingJsonPath("$.password")), 200, "{\"id\":4,\"token\":\"QpwL5tke4Pnpja7X4\"}");
        for (String path : List.of("/api/login", "/api/register")) {
            json(s, post(urlPathEqualTo(path)).atPriority(5)
                    .withRequestBody(notMatching(".*\"password\".*")), 400, "{\"error\":\"Missing password\"}");
            json(s, post(urlPathEqualTo(path)).atPriority(10), 400, "{\"error\":\"user not found\"}");
        }
    }
}
