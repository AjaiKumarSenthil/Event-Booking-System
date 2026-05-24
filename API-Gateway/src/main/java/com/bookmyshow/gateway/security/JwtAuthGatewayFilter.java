package com.bookmyshow.gateway.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    private final JwtUtil jwtUtil;

    @Value("${gateway.security.forbidden-patterns:}")
    private List<String> forbiddenPatternStrings;

    @Value("${gateway.security.public-paths:}")
    private List<String> publicPathStrings;

    @Value("${gateway.security.public-get-patterns:}")
    private List<String> publicGetPatternStrings;

    private List<PathPattern> forbiddenPatterns;
    private List<PathPattern> publicPaths;
    private List<PathPattern> publicGetPatterns;

    @PostConstruct
    void init() {
        this.forbiddenPatterns = forbiddenPatternStrings.stream().map(PARSER::parse).toList();
        this.publicPaths = publicPathStrings.stream().map(PARSER::parse).toList();
        this.publicGetPatterns = publicGetPatternStrings.stream().map(PARSER::parse).toList();
        log.info("Gateway security loaded: {} forbidden, {} public, {} public-GET patterns",
                forbiddenPatterns.size(), publicPaths.size(), publicGetPatterns.size());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        PathContainer path = request.getPath().pathWithinApplication();
        HttpMethod method = request.getMethod();

        if (matchesAny(forbiddenPatterns, path)) {
            log.warn("Blocked attempt to reach forbidden path via gateway: {} {}", method, path.value());
            return notFound(exchange);
        }

        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Rejected request without Bearer token: {} {}", method, path.value());
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Rejected request with invalid JWT: {} {}", method, path.value());
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private boolean isPublic(PathContainer path, HttpMethod method) {
        if (matchesAny(publicPaths, path)) {
            return true;
        }
        return HttpMethod.GET.equals(method) && matchesAny(publicGetPatterns, path);
    }

    private static boolean matchesAny(List<PathPattern> patterns, PathContainer path) {
        for (PathPattern pattern : patterns) {
            if (pattern.matches(path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> notFound(ServerWebExchange exchange) {
        // 404 (not 403) is intentional — we do not advertise the existence of
        // forbidden endpoints to external callers.
        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        return exchange.getResponse().setComplete();
    }
}
