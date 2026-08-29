package dev.devangsharma.gatekeeper.common.event;

import java.time.Instant;

public record RateLimitEvent(
        String clientId,
        String routeId,
        String method,
        String path,
        long requestCost,
        long currentCount,
        long limit,
        boolean allowed,
        Instant timestamp
) {
    public RateLimitEvent {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
