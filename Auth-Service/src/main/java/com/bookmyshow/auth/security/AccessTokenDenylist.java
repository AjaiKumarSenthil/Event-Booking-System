package com.bookmyshow.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Records the {@code jti} of an access token that has been logged out so the
 * gateway can reject it before it naturally expires.
 *
 * <p>Stateless JWTs cannot be invalidated by signature alone — revoking the
 * refresh token only stops <em>new</em> access tokens from being minted. To make
 * logout immediate we keep a short-lived denylist in Redis, keyed by {@code jti}
 * and given a TTL equal to the token's remaining lifetime, so the entry cleans
 * itself up once the token would have expired anyway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenDenylist {

    /** Must match the prefix the API gateway checks against. */
    public static final String KEY_PREFIX = "auth:denylist:access:";

    private final StringRedisTemplate redis;
    private final JwtService jwtService;

    public void denylist(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            String jti = jwtService.getJti(accessToken);
            if (jti == null || jti.isBlank()) {
                log.warn("Access token has no jti claim; cannot denylist (token predates jti rollout)");
                return;
            }
            long ttlSeconds = Duration.between(Instant.now(), jwtService.getExpiration(accessToken)).getSeconds();
            if (ttlSeconds <= 0) {
                return;
            }
            redis.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
            log.info("Access token denylisted jti={} ttlSeconds={}", jti, ttlSeconds);
        } catch (Exception e) {
            // A malformed/expired access token on logout is harmless — it already can't be used.
            log.warn("Skipped denylisting access token on logout: {}", e.getMessage());
        }
    }
}
