package com.bookmyshow.booking.client.dto;

import java.util.UUID;

public record LockedSeat(UUID showSeatId, String rowLabel, Integer seatNumber) {
}
