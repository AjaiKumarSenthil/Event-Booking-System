package com.bookmyshow.gateway.ratelimit;

import com.bookmyshow.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * KeyResolver beans referenced by name (#{@ipKeyResolver}, #{@userIdKeyResolver})
 * from the RequestRateLimiter filter args in application.yaml.
 *
 * <p>Returned keys are namespaced ("ip:" / "user:") so a logged-in user and an
 * anonymous caller from the same address never share a token bucket.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeyResolvers {

    private static final String UNKNOWN_KEY = "ip:unknown";

    private final JwtUtil jwtUtil;

    /** IP-based key. Used on the public auth routes where no JWT exists yet. */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just("ip:" + resolveIp(exchange.getRequest()));
    }

    /**
     * User-id-based key (JWT {@code sub} claim). Falls back to IP for unauthenticated
     * requests so an attacker can't bypass the limit simply by dropping the token —
     * those requests would be rejected by the JWT filter anyway.
     */
    @Bean
    public KeyResolver userIdKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    try {
                        String userId = jwtUtil.getUserId(token);
                        if (userId != null && !userId.isBlank()) {
                            return Mono.just("user:" + userId);
                        }
                    } catch (Exception ex) {
                        log.debug("Failed to extract sub from JWT for rate-limit key, falling back to IP", ex);
                    }
                }
            }
            return Mono.just("ip:" + resolveIp(exchange.getRequest()));
        };
    }

    /**
     * Prefer the leftmost entry in {@code X-Forwarded-For} for proxy-deployed setups,
     * since {@link ServerHttpRequest#getRemoteAddress()} would otherwise return the
     * load balancer's address for every caller.
     */
    private static String resolveIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = (comma >= 0 ? forwarded.substring(0, comma) : forwarded).trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        InetSocketAddress remote = request.getRemoteAddress();
        if (remote == null || remote.getAddress() == null) {
            return UNKNOWN_KEY.substring("ip:".length());
        }
        return remote.getAddress().getHostAddress();
    }
}
