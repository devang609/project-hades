package dev.devangsharma.gatekeeper.gateway.config;

import dev.devangsharma.gatekeeper.gateway.proxy.ProxyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> proxyRoutes(ProxyHandler proxyHandler) {
        return RouterFunctions.route()
                .path("/**", builder -> builder
                        .GET("/**", proxyHandler)
                        .POST("/**", proxyHandler)
                        .PUT("/**", proxyHandler)
                        .PATCH("/**", proxyHandler)
                        .DELETE("/**", proxyHandler))
                .build();
    }
}
