package com.bookmyshow.notification.service;

import com.bookmyshow.notification.entity.NotificationLog;
import com.bookmyshow.notification.enums.NotificationStatus;
import com.bookmyshow.notification.event.BookingEvent;
import com.bookmyshow.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    /**
     * Idempotent handler for a BookingEvent. Returns true if the email was sent
     * during this invocation, false if it was already sent earlier (duplicate
     * Kafka delivery). Throws on a real send failure so that the consumer
     * error handler can retry / DLT it.
     */
    @Transactional
    public boolean process(BookingEvent event) {
        if (event.getBookingId() == null || event.getEventType() == null) {
            log.warn("Skipping malformed booking event: {}", event);
            return false;
        }

        var existing = notificationLogRepository
                .findByBookingIdAndEventType(event.getBookingId(), event.getEventType());

        if (existing.isPresent() && existing.get().getStatus() == NotificationStatus.SENT) {
            log.info("Notification already sent for booking {} event {} - skipping",
                    event.getBookingId(), event.getEventType());
            return false;
        }

        NotificationLog logEntry = existing.orElseGet(NotificationLog::new);
        logEntry.setBookingId(event.getBookingId());
        logEntry.setEventType(event.getEventType());
        logEntry.setRecipient(event.getUserEmail());
        logEntry.setStatus(NotificationStatus.PENDING);
        if (logEntry.getCreatedAt() == null) {
            logEntry.setCreatedAt(LocalDateTime.now());
        }
        logEntry = notificationLogRepository.save(logEntry);

        try {
            emailService.sendBookingEmail(event);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(LocalDateTime.now());
            logEntry.setError(null);
            notificationLogRepository.save(logEntry);
            return true;
        } catch (Exception ex) {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setError(truncate(ex.getMessage()));
            notificationLogRepository.save(logEntry);
            throw new NotificationDeliveryException(
                    "Failed to send " + event.getEventType() + " email for booking " + event.getBookingId(), ex);
        }
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }

    public static class NotificationDeliveryException extends RuntimeException {
        public NotificationDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
