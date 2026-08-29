package dev.devangsharma.gatekeeper.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gatekeeper.routes")
public record RouteProperties(List<Route> definitions) {

    public RouteProperties {
        if (definitions == null) {
            definitions = List.of();
        }
    }

    public Route resolve(String path) {
        for (Route route : definitions) {
            if (path.startsWith(route.prefix())) {
                return route;
            }
        }
        return null;
    }

    public record Route(String id, String prefix, String target) {
        public Route {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Route id must not be blank");
            }
            if (prefix == null || prefix.isBlank()) {
                throw new IllegalArgumentException("Route prefix must not be blank");
            }
            if (target == null || target.isBlank()) {
                throw new IllegalArgumentException("Route target must not be blank");
            }
        }
    }
}
