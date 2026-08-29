package dev.devangsharma.gatekeeper.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class SlidingWindowRateLimitIT {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static LettuceConnectionFactory connectionFactory;
    static ReactiveRedisTemplate<String, String> redisTemplate;
    static RedisScript<List> script;

    @BeforeAll
    static void setupRedis() {
        connectionFactory = new LettuceConnectionFactory(
                redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new ReactiveRedisTemplate<>(
                connectionFactory, RedisSerializationContext.string());

        script = RedisScript.of(new ClassPathResource("scripts/sliding_window_cost.lua"), List.class);
    }

    @AfterAll
    static void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void flushRedis() {
        redisTemplate.getConnectionFactory().getReactiveConnection()
                .serverCommands().flushAll().block();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String key = "rl:{test-client}:{test-route}";
        long limit = 5;

        for (int i = 0; i < 5; i++) {
            List<Long> result = executeScript(key, limit);
            assertEquals(1L, result.get(0), "Request " + i + " should be allowed");
        }

        List<Long> denied = executeScript(key, limit);
        assertEquals(0L, denied.get(0), "6th request should be denied");
        assertEquals(5L, denied.get(1), "Current count should be 5");
    }

    @Test
    void shouldRespectSlidingWindow() throws InterruptedException {
        String key = "rl:{window-test}:{route}";
        long limit = 2;
        long windowMicros = 1_000_000; // 1 second window

        executeScript(key, limit, windowMicros);
        executeScript(key, limit, windowMicros);

        List<Long> denied = executeScript(key, limit, windowMicros);
        assertEquals(0L, denied.get(0), "3rd request should be denied within window");

        Thread.sleep(1100);

        List<Long> allowed = executeScript(key, limit, windowMicros);
        assertEquals(1L, allowed.get(0), "Request after window expiry should be allowed");
    }

    @Test
    void shouldMaintainAtomicityUnderConcurrentLoad() {
        String key = "rl:{concurrent-test}:{route}";
        long limit = 50;

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger deniedCount = new AtomicInteger(0);

        Flux.range(0, 100)
                .flatMap(i -> {
                    String requestId = UUID.randomUUID().toString();
                    long nowMicros = Instant.now().toEpochMilli() * 1000;
                    long windowMicros = 60_000_000L;

                    return redisTemplate.execute(
                            script,
                            List.of(key),
                            String.valueOf(nowMicros),
                            String.valueOf(windowMicros),
                            String.valueOf(limit),
                            "1",
                            requestId
                    ).next().doOnNext(raw -> {
                        @SuppressWarnings("unchecked")
                        List<Long> result = (List<Long>) raw;
                        if (result.get(0) == 1L) {
                            allowedCount.incrementAndGet();
                        } else {
                            deniedCount.incrementAndGet();
                        }
                    });
                }, 20)
                .collectList()
                .block(Duration.ofSeconds(10));

        assertEquals(50, allowedCount.get(),
                "Exactly 50 requests should be allowed (limit=" + limit + ")");
        assertEquals(50, deniedCount.get(),
                "Exactly 50 requests should be denied");
    }

    @Test
    void shouldReturnCorrectRateLimitHeaders() {
        String key = "rl:{header-test}:{route}";
        long limit = 10;

        List<Long> result = executeScript(key, limit);

        assertEquals(1L, result.get(0)); // allowed
        assertEquals(1L, result.get(1)); // current count
        assertEquals(10L, result.get(2)); // limit
        assertTrue(result.get(3) > 0); // reset epoch seconds
    }

    private List<Long> executeScript(String key, long limit) {
        return executeScript(key, limit, 60_000_000L);
    }

    @SuppressWarnings("unchecked")
    private List<Long> executeScript(String key, long limit, long windowMicros) {
        long nowMicros = Instant.now().toEpochMilli() * 1000;
        String requestId = UUID.randomUUID().toString();

        return (List<Long>) redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(nowMicros),
                String.valueOf(windowMicros),
                String.valueOf(limit),
                "1",
                requestId
        ).next().block(Duration.ofSeconds(5));
    }
}
