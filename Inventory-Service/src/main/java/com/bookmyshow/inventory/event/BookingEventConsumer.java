package com.bookmyshow.inventory.event;

import com.bookmyshow.inventory.show.service.IShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final IShowService showService;

    @KafkaListener(topics = {"booking.confirmed", "booking.cancelled"}, groupId = "inventory-service-group")
    public void consume(BookingEvent bookingEvent) {
        log.info("Received booking event: type={}, bookingId={}, showId={}",
                bookingEvent.getEventType(), bookingEvent.getBookingId(), bookingEvent.getShowId());

        if ("BOOKING_CONFIRMED".equals(bookingEvent.getEventType())) {
            showService.confirmSeats(bookingEvent.getShowId(), bookingEvent.getShowSeatIds());
        } else {
            showService.releaseSeats(bookingEvent.getShowId(), bookingEvent.getShowSeatIds());
        }
    }
}
