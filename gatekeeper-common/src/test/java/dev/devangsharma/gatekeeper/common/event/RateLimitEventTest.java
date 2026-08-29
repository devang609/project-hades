package dev.devangsharma.gatekeeper.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitEventTest {

    @Test
    void shouldCreateEventWithAllFields() {
        var now = Instant.now();
        var event = new RateLimitEvent("client-1", "route-1", "GET", "/api/test", 1, 5, 100, true, now);

        assertEquals("client-1", event.clientId());
        assertEquals(5, event.currentCount());
        assertTrue(event.allowed());
        assertEquals(now, event.timestamp());
    }

    @Test
    void shouldAutoPopulateTimestamp() {
        var before = Instant.now();
        var event = new RateLimitEvent("client-1", "route-1", "GET", "/api/test", 1, 5, 100, true, null);
        var after = Instant.now();

        assertNotNull(event.timestamp());
        assertFalse(event.timestamp().isBefore(before));
        assertFalse(event.timestamp().isAfter(after));
    }

    @Test
    void shouldRejectBlankClientId() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimitEvent("", "route-1", "GET", "/api/test", 1, 0, 100, true, null));
    }
}
