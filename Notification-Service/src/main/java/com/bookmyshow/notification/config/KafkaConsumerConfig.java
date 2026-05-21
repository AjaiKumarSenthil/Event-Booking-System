package com.bookmyshow.notification.config;

import com.bookmyshow.notification.event.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Wires a typed listener container factory used by {@code @KafkaListener(containerFactory = "bookingEventListenerFactory")}
 * along with a retry + DLT error handler.
 *
 * <p>The underlying {@link ConsumerFactory} is the one auto-configured by Spring Boot
 * from {@code spring.kafka.consumer.*} properties (JSON deserializer + default type =
 * {@link BookingEvent}). We only override the error handling behaviour here.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${notification.topics.dead-letter}")
    private String deadLetterTopic;

    @Value("${notification.retry.max-attempts}")
    private long maxAttempts;

    @Value("${notification.retry.initial-interval-ms}")
    private long initialIntervalMs;

    @Value("${notification.retry.multiplier}")
    private double multiplier;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingEvent> bookingEventListenerFactory(
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            DefaultErrorHandler bookingEventErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        @SuppressWarnings({"unchecked", "rawtypes"})
        ConsumerFactory<String, BookingEvent> typed = (ConsumerFactory) kafkaConsumerFactory;
        factory.setConsumerFactory(typed);
        factory.setCommonErrorHandler(bookingEventErrorHandler);
        return factory;
    }

    @Bean
    public DefaultErrorHandler bookingEventErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    log.error("Routing failed booking event to DLT [{}] (key={}): {}",
                            deadLetterTopic, record.key(), ex.getMessage());
                    return new TopicPartition(deadLetterTopic, -1);
                });

        ExponentialBackOff backOff = new ExponentialBackOff(initialIntervalMs, multiplier);
        // maxAttempts counts the initial delivery + retries; subtract 1 to get retry attempts
        backOff.setMaxAttempts(Math.max(0, maxAttempts - 1));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.setCommitRecovered(true);
        handler.setLogLevel(KafkaException.Level.WARN);
        return handler;
    }
}
