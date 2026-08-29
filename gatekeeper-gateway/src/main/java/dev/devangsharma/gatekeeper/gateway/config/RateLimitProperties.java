package dev.devangsharma.gatekeeper.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "gatekeeper.rate-limit")
public record RateLimitProperties(
        long defaultLimit,
        Duration defaultWindow,
        String keyPrefix
) {
    public RateLimitProperties {
        if (defaultLimit <= 0) defaultLimit = 100;
        if (defaultWindow == null) defaultWindow = Duration.ofMinutes(1);
        if (keyPrefix == null || keyPrefix.isBlank()) keyPrefix = "rl";
    }
}
