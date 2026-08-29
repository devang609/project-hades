package dev.devangsharma.gatekeeper.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<List> slidingWindowScript() {
        return RedisScript.of(new ClassPathResource("scripts/sliding_window_cost.lua"), List.class);
    }
}
