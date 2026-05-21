package com.bookmyshow.notification.consumer;

import com.bookmyshow.notification.event.BookingEvent;
import com.bookmyshow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = {"${notification.topics.booking-confirmed}", "${notification.topics.booking-cancelled}"},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "bookingEventListenerFactory")
    public void onBookingEvent(@Payload BookingEvent event,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received booking event from topic={} partition={} offset={} bookingId={} type={}",
                topic, partition, offset, event.getBookingId(), event.getEventType());

        notificationService.process(event);
    }
}
