package com.bookmyshow.notification.repository;

import com.bookmyshow.notification.entity.NotificationLog;
import com.bookmyshow.notification.enums.BookingEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Optional<NotificationLog> findByBookingIdAndEventType(UUID bookingId, BookingEventType eventType);
}
