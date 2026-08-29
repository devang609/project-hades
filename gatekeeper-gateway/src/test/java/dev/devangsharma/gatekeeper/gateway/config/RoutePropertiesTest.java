package dev.devangsharma.gatekeeper.gateway.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutePropertiesTest {

    @Test
    void shouldResolveMatchingRoute() {
        var route = new RouteProperties.Route("demo", "/demo", "https://httpbin.org");
        var props = new RouteProperties(List.of(route));

        var matched = props.resolve("/demo/get");
        assertNotNull(matched);
        assertEquals("demo", matched.id());
        assertEquals("https://httpbin.org", matched.target());
    }

    @Test
    void shouldReturnNullForUnmatchedPath() {
        var route = new RouteProperties.Route("demo", "/demo", "https://httpbin.org");
        var props = new RouteProperties(List.of(route));

        assertNull(props.resolve("/api/users"));
    }

    @Test
    void shouldMatchFirstRouteInOrder() {
        var routes = List.of(
                new RouteProperties.Route("api", "/api", "http://api-service:8080"),
                new RouteProperties.Route("demo", "/demo", "https://httpbin.org")
        );
        var props = new RouteProperties(routes);

        assertEquals("api", props.resolve("/api/v1/clients").id());
    }

    @Test
    void shouldRejectBlankFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new RouteProperties.Route("", "/demo", "https://httpbin.org"));
        assertThrows(IllegalArgumentException.class,
                () -> new RouteProperties.Route("demo", "", "https://httpbin.org"));
        assertThrows(IllegalArgumentException.class,
                () -> new RouteProperties.Route("demo", "/demo", ""));
    }

    @Test
    void shouldDefaultToEmptyList() {
        var props = new RouteProperties(null);
        assertNotNull(props.definitions());
        assertTrue(props.definitions().isEmpty());
    }
}
