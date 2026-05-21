package com.bookmyshow.inventory.show.controller;

import com.bookmyshow.inventory.api.InternalApi;
import com.bookmyshow.inventory.model.SeatLockRequest;
import com.bookmyshow.inventory.model.SeatLockResponse;
import com.bookmyshow.inventory.model.SeatStatusUpdateRequest;
import com.bookmyshow.inventory.show.service.IShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalController implements InternalApi {

    private final IShowService showService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatLockResponse> lockSeats(UUID showId, SeatLockRequest seatLockRequest) {
        return ResponseEntity.ok(showService.lockSeats(showId, seatLockRequest.getSeats()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> releaseSeats(UUID showId, SeatStatusUpdateRequest request) {
        showService.releaseSeats(showId, request.getShowSeatIds());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmSeats(UUID showId, SeatStatusUpdateRequest request) {
        showService.confirmSeats(showId, request.getShowSeatIds());
        return ResponseEntity.ok().build();
    }
}
