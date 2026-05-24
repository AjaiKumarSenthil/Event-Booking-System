package com.bookmyshow.booking.client;

import com.bookmyshow.booking.client.dto.SeatLockResponse;
import com.bookmyshow.booking.exception.ResourceNotFoundException;
import com.bookmyshow.booking.exception.SeatNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl,
                           BearerTokenForwardingInterceptor authForwardingInterceptor) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(authForwardingInterceptor)
                .build();
    }

    public SeatLockResponse lockSeats(UUID showId, List<Map<String, Object>> seats) {
        return restClient.post()
                .uri("/internal/shows/{showId}/seats/lock", showId)
                .body(Map.of("seats", seats))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 404) {
                        log.warn("Inventory lockSeats 404: showId={}", showId);
                        throw new ResourceNotFoundException("Show or seats not found in inventory");
                    }
                    if (res.getStatusCode().value() == 409) {
                        log.warn("Inventory lockSeats 409 (seat conflict): showId={} seatCount={}", showId, seats.size());
                        throw new SeatNotAvailableException("One or more seats are no longer available");
                    }
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("Inventory lockSeats 5xx: showId={} status={}", showId, res.getStatusCode());
                })
                .body(SeatLockResponse.class);
    }

    public void releaseSeats(UUID showId, List<UUID> showSeatIds) {
        restClient.post()
                .uri("/internal/shows/{showId}/seats/release", showId)
                .body(Map.of("showSeatIds", showSeatIds))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.warn("Inventory releaseSeats failed: showId={} seatCount={} status={}",
                            showId, showSeatIds.size(), res.getStatusCode());
                })
                .toBodilessEntity();
    }

    public void confirmSeats(UUID showId, List<UUID> showSeatIds) {
        restClient.post()
                .uri("/internal/shows/{showId}/seats/confirm", showId)
                .body(Map.of("showSeatIds", showSeatIds))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.warn("Inventory confirmSeats failed: showId={} seatCount={} status={}",
                            showId, showSeatIds.size(), res.getStatusCode());
                })
                .toBodilessEntity();
    }
}
