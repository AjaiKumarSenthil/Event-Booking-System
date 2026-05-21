package com.bookmyshow.booking.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SeatLockResponse(
        UUID showId,
        String theatreName,
        String screenName,
        String movieTitle,
        LocalDateTime showTime,
        BigDecimal price,
        List<LockedSeat> lockedSeats
) {
}
