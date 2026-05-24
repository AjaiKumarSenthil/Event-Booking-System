package com.bookmyshow.inventory.show.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Industry-standard short-lived seat holds backed by Redis.
 *
 * <p>The hold is the authoritative "this seat is being checked out" state —
 * Postgres only stores {@code AVAILABLE} or {@code BOOKED}. A hold lives for
 * {@code inventory.seat-hold.ttl-seconds} and is removed by Redis automatically
 * if the user abandons the booking, which is how we get auto-expiry without a
 * scheduled sweeper.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SeatHoldRedisRepository {

    private static final String KEY_PREFIX = "seat:hold:";
    private static final String HOLD_MARKER = "1";

    private final StringRedisTemplate redis;

    @Value("${inventory.seat-hold.ttl-seconds:600}")
    private long ttlSeconds;

    private DefaultRedisScript<Long> holdScript;

    @PostConstruct
    void initScripts() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/hold-seats.lua")));
        script.setResultType(Long.class);
        this.holdScript = script;
    }

    /**
     * Reserve every seat or none. Returns {@code true} iff all keys were freshly
     * created. Partial acquisitions are rolled back inside the Lua script so a
     * caller can safely retry without leaking holds.
     */
    public boolean tryHoldAll(Collection<UUID> showSeatIds) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return true;
        }
        List<String> keys = toKeys(showSeatIds);
        Long result = redis.execute(
                holdScript,
                keys,
                String.valueOf(ttlSeconds),
                HOLD_MARKER
        );
        return result != null && result == 1L;
    }

    /** Remove hold keys. Idempotent; safe to call on already-expired keys. */
    public void release(Collection<UUID> showSeatIds) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return;
        }
        redis.delete(toKeys(showSeatIds));
    }

    /**
     * Of the given seat ids, returns those currently held. Uses {@code MGET}
     * for a single round-trip so {@code getShowSeats} can overlay holds onto
     * the DB seat map without N+1 calls.
     */
    public Set<UUID> heldAmong(Collection<UUID> showSeatIds) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<UUID> ordered = new ArrayList<>(showSeatIds);
        List<String> keys = toKeys(ordered);
        List<String> values = redis.opsForValue().multiGet(keys);
        if (values == null) {
            return Collections.emptySet();
        }
        Set<UUID> held = new HashSet<>();
        for (int i = 0; i < ordered.size(); i++) {
            if (values.get(i) != null) {
                held.add(ordered.get(i));
            }
        }
        return held;
    }

    private static List<String> toKeys(Collection<UUID> ids) {
        return ids.stream().map(id -> KEY_PREFIX + id).toList();
    }
}
