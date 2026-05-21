package com.bookmyshow.booking.client;

import com.bookmyshow.booking.client.dto.SeatLockResponse;
import com.bookmyshow.booking.exception.ResourceNotFoundException;
import com.bookmyshow.booking.exception.SeatNotAvailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                        throw new ResourceNotFoundException("Show or seats not found in inventory");
                    }
                    if (res.getStatusCode().value() == 409) {
                        throw new SeatNotAvailableException("One or more seats are no longer available");
                    }
                })
                .body(SeatLockResponse.class);
    }

    public void releaseSeats(UUID showId, List<UUID> showSeatIds) {
        restClient.post()
                .uri("/internal/shows/{showId}/seats/release", showId)
                .body(Map.of("showSeatIds", showSeatIds))
                .retrieve()
                .toBodilessEntity();
    }

    public void confirmSeats(UUID showId, List<UUID> showSeatIds) {
        restClient.post()
                .uri("/internal/shows/{showId}/seats/confirm", showId)
                .body(Map.of("showSeatIds", showSeatIds))
                .retrieve()
                .toBodilessEntity();
    }
}
