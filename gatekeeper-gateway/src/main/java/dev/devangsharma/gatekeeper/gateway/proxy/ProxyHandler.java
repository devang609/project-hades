package dev.devangsharma.gatekeeper.gateway.proxy;

import dev.devangsharma.gatekeeper.gateway.config.RouteProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ProxyHandler implements HandlerFunction<ServerResponse> {

    private final WebClient webClient;
    private final RouteProperties routeProperties;

    public ProxyHandler(WebClient.Builder webClientBuilder, RouteProperties routeProperties) {
        this.webClient = webClientBuilder.build();
        this.routeProperties = routeProperties;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        String path = request.path();
        RouteProperties.Route matched = routeProperties.resolve(path);

        if (matched == null) {
            return ServerResponse.notFound().build();
        }

        String strippedPath = path.substring(matched.prefix().length());
        if (!strippedPath.startsWith("/")) {
            strippedPath = "/" + strippedPath;
        }

        URI targetUri = URI.create(matched.target() + strippedPath);
        HttpMethod method = request.method();

        WebClient.RequestBodySpec spec = webClient
                .method(method)
                .uri(targetUri)
                .headers(headers -> copyHeaders(request.headers().asHttpHeaders(), headers));

        Mono<WebClient.RequestHeadersSpec<?>> requestMono;
        if (requiresBody(method)) {
            requestMono = Mono.just(
                    spec.body(BodyInserters.fromDataBuffers(request.bodyToFlux(DataBuffer.class))));
        } else {
            requestMono = Mono.just(spec);
        }

        return requestMono.flatMap(req ->
                req.exchangeToMono(clientResponse ->
                        ServerResponse.status(clientResponse.statusCode())
                                .headers(h -> h.addAll(filterResponseHeaders(
                                        clientResponse.headers().asHttpHeaders())))
                                .body(BodyInserters.fromDataBuffers(
                                        clientResponse.bodyToFlux(DataBuffer.class)))
                )
        );
    }

    private boolean requiresBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Host") && !name.equalsIgnoreCase("Content-Length")) {
                target.addAll(name, values);
            }
        });
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders filtered = new HttpHeaders();
        source.forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Transfer-Encoding")) {
                filtered.addAll(name, values);
            }
        });
        return filtered;
    }
}
