package dev.devangsharma.gatekeeper.gateway.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitPropertiesTest {

    @Test
    void shouldApplyDefaults() {
        var props = new RateLimitProperties(0, null, null);
        assertEquals(100, props.defaultLimit());
        assertEquals(Duration.ofMinutes(1), props.defaultWindow());
        assertEquals("rl", props.keyPrefix());
    }

    @Test
    void shouldPreserveExplicitValues() {
        var props = new RateLimitProperties(50, Duration.ofSeconds(30), "custom");
        assertEquals(50, props.defaultLimit());
        assertEquals(Duration.ofSeconds(30), props.defaultWindow());
        assertEquals("custom", props.keyPrefix());
    }
}
