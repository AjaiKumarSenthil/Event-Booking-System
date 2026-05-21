package com.bookmyshow.booking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public void publishBookingConfirmation(BookingEvent bookingEvent) {
        sendEvent("booking.confirmed", bookingEvent);
    }

    public void publishBookingCancelled(BookingEvent bookingEvent) {
        sendEvent("booking.cancelled", bookingEvent);
    }

    private void sendEvent(String topic, BookingEvent event) {
        kafkaTemplate.send(topic, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send event to topic [{}]: {}", topic, ex.getMessage(), ex);
                    } else {
                        log.info("Event sent to topic [{}] partition [{}] offset [{}]",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
