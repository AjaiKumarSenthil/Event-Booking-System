package com.bookmyshow.booking.event;

import com.bookmyshow.booking.enums.BookingEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {

    private BookingEventType eventType;
    private UUID bookingId;
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private LocalDateTime showTime;
    private List<UUID> showSeatIds;
    private List<String> seatLabels;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}