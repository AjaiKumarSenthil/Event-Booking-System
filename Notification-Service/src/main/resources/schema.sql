-- ================================================================
-- Notification Service - Database Schema (PostgreSQL 13+)
-- ================================================================
-- Owns: notification_log
--
-- Tracks every booking-event-driven notification attempt for
-- idempotency (de-dupe Kafka redeliveries) and operational audit.
-- ================================================================

CREATE TABLE notification_log (
    id          UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    booking_id  UUID            NOT NULL,
    event_type  VARCHAR(40)     NOT NULL,
    recipient   VARCHAR(255)    NOT NULL,
    status      VARCHAR(20)     NOT NULL,
    error       TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),
    sent_at     TIMESTAMP,
    CONSTRAINT uk_notification_booking_event UNIQUE (booking_id, event_type)
);

CREATE INDEX idx_notification_status ON notification_log(status);
CREATE INDEX idx_notification_booking ON notification_log(booking_id);
