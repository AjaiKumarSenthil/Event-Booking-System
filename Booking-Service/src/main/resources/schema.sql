-- ================================================================
-- Booking Service — Database Schema (PostgreSQL 13+)
-- ================================================================
-- Owns: booking, booking_seat
--
-- show_id and show_seat_id are stored as plain UUIDs.
-- No FK references to inventory tables — data integrity is enforced
-- at the application level via REST calls to the inventory service.
-- ================================================================

CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED');

CREATE TABLE booking (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID            NOT NULL,
    user_email      VARCHAR(255)    NOT NULL,
    user_name       VARCHAR(200)    NOT NULL,
    show_id         UUID            NOT NULL,
    theatre_name    VARCHAR(200)    NOT NULL,
    screen_name     VARCHAR(50)     NOT NULL,
    movie_title     VARCHAR(300)    NOT NULL,
    show_time       TIMESTAMP       NOT NULL,
    total_amount    DECIMAL(10,2)   NOT NULL,
    status          booking_status  NOT NULL DEFAULT 'PENDING',
    payment_id      VARCHAR(100),
    payment_method  VARCHAR(50),
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    expires_at      TIMESTAMP       NOT NULL
);

CREATE TABLE booking_seat (
    id              UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    booking_id      UUID        NOT NULL REFERENCES booking(id) ON DELETE CASCADE,
    show_seat_id    UUID        NOT NULL,
    row_label       VARCHAR(2)  NOT NULL,
    seat_number     INT         NOT NULL,
    UNIQUE (booking_id, show_seat_id)
);

-- ── Indexes ────────────────────────────────────────────────

CREATE INDEX idx_booking_user         ON booking(user_id);
CREATE INDEX idx_booking_show         ON booking(show_id);
CREATE INDEX idx_booking_status       ON booking(status, expires_at);
CREATE INDEX idx_booking_seat_booking ON booking_seat(booking_id);
