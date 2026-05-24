package com.bookmyshow.inventory.event;

import com.bookmyshow.inventory.show.service.IShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final IShowService showService;

    @KafkaListener(topics = {"booking.confirmed", "booking.cancelled"}, groupId = "inventory-service-group")
    public void consume(@Payload BookingEvent bookingEvent,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received booking event from topic={} partition={} offset={} type={} bookingId={} showId={}",
                topic, partition, offset, bookingEvent.getEventType(),
                bookingEvent.getBookingId(), bookingEvent.getShowId());

        try {
            if ("BOOKING_CONFIRMED".equals(bookingEvent.getEventType())) {
                showService.confirmSeats(bookingEvent.getShowId(), bookingEvent.getShowSeatIds());
            } else {
                showService.releaseSeats(bookingEvent.getShowId(), bookingEvent.getShowSeatIds());
            }
        } catch (Exception ex) {
            log.error("Failed to process booking event bookingId={} showId={} type={}: {}",
                    bookingEvent.getBookingId(), bookingEvent.getShowId(),
                    bookingEvent.getEventType(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
