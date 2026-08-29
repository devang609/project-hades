package dev.devangsharma.gatekeeper.gateway.filter;

import dev.devangsharma.gatekeeper.common.dto.RateLimitResult;
import dev.devangsharma.gatekeeper.gateway.config.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class RateLimitWebFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitWebFilter.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> slidingWindowScript;
    private final RateLimitProperties properties;

    public RateLimitWebFilter(
            ReactiveRedisTemplate<String, String> redisTemplate,
            RedisScript<List> slidingWindowScript,
            RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.slidingWindowScript = slidingWindowScript;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        String clientId = resolveClientId(exchange.getRequest().getHeaders());
        String key = properties.keyPrefix() + ":{" + clientId + "}";
        long nowMicros = Instant.now().toEpochMilli() * 1000;
        long windowMicros = properties.defaultWindow().toMillis() * 1000;
        String requestId = UUID.randomUUID().toString();

        return redisTemplate.execute(
                slidingWindowScript,
                List.of(key),
                String.valueOf(nowMicros),
                String.valueOf(windowMicros),
                String.valueOf(properties.defaultLimit()),
                "1",
                requestId
        ).next().flatMap(rawResult -> {
            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) rawResult;

            boolean allowed = result.get(0) == 1L;
            long currentCount = result.get(1);
            long limit = result.get(2);
            long resetEpochSeconds = result.get(3);

            RateLimitResult rateLimitResult = allowed
                    ? RateLimitResult.allowed(currentCount, limit, resetEpochSeconds)
                    : RateLimitResult.denied(currentCount, limit, resetEpochSeconds);

            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
            responseHeaders.set("X-RateLimit-Limit", String.valueOf(limit));
            responseHeaders.set("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));
            responseHeaders.set("X-RateLimit-Reset", String.valueOf(resetEpochSeconds));

            if (!allowed) {
                long retryAfter = Math.max(1, resetEpochSeconds - Instant.now().getEpochSecond());
                responseHeaders.set("Retry-After", String.valueOf(retryAfter));
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                log.info("Rate limit exceeded for client={} path={} count={} limit={}",
                        clientId, path, currentCount, limit);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveClientId(HttpHeaders headers) {
        String apiKey = headers.getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        String forwarded = headers.getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return "anonymous";
    }
}
