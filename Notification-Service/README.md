# Notification-Service

Consumes `booking.confirmed` and `booking.cancelled` Kafka events published by `Booking-Service`
and sends HTML emails to the user via Gmail SMTP.

- Port: `8084`
- Context path: `/notification/v1`
- Topics consumed: `booking.confirmed`, `booking.cancelled`
- DLT: `booking.notifications.dlt`
- Group id: `notification-service`

## How it works

```
Booking-Service --(Kafka)--> Notification-Service --(SMTP)--> Gmail --> user inbox
                                     |
                                     +--> notification_log (Postgres)
```

The `BookingEvent` is self-contained: it already carries `userEmail`, `userName`, movie
title, theatre, screen, show time, seats, and total amount. The service never calls
Auth-Service or Booking-Service at runtime.

Idempotency is enforced by `notification_log(booking_id, event_type)` UNIQUE constraint:
duplicate Kafka deliveries are skipped if a `SENT` row already exists. Failures are retried
with exponential back-off (configurable in `application.yaml`); after the final attempt
the record is published to the DLT for manual inspection / replay.

## Prerequisites

1. Kafka running on `localhost:9092` (start with the repo's `docker-compose.yml`).
2. PostgreSQL running on `localhost:5432` with database `bookmyshow` and user `catalog/catalog`
   (same shared instance used by Auth/Booking/Catalog services).
3. The `notification_log` table created. Run [src/main/resources/schema.sql](src/main/resources/schema.sql)
   against the `bookmyshow` database once:

   ```bash
   psql -h localhost -U catalog -d bookmyshow -f src/main/resources/schema.sql
   ```

4. The new columns on the `booking` table in `Booking-Service` (added in this change):

   ```sql
   ALTER TABLE booking ADD COLUMN user_email VARCHAR(255) NOT NULL DEFAULT '';
   ALTER TABLE booking ADD COLUMN user_name  VARCHAR(200) NOT NULL DEFAULT '';
   ```

   (Or simply drop and recreate the schema from `Booking-Service/src/main/resources/schema.sql`
   if you have no existing data.)

## Gmail SMTP setup

Gmail does not accept your regular Google account password from SMTP. You must use an
**App Password**:

1. Enable **2-Step Verification** on the Google account you want to send from:
   https://myaccount.google.com/security
2. Go to https://myaccount.google.com/apppasswords
3. Create an app password (any name works, e.g. `BookMyShow Local`). Google will display
   a **16-character** password — copy it (no spaces).
4. Export both env vars **before starting the service** (in the same terminal):

   PowerShell:

   ```powershell
   $env:MAIL_USERNAME = "you@gmail.com"
   $env:MAIL_APP_PASSWORD = "xxxxxxxxxxxxxxxx"   # 16-char app password, no spaces
   ```

   bash / zsh:

   ```bash
   export MAIL_USERNAME="you@gmail.com"
   export MAIL_APP_PASSWORD="xxxxxxxxxxxxxxxx"
   ```

5. (Optional) The `From:` address defaults to `MAIL_USERNAME`. Override with
   `--notification.from-email=...` if you want a different display address.

> **Limits:** Gmail free accounts cap outbound mail at ~500 messages/day. Fine for
> local dev; swap for SendGrid / AWS SES / Mailgun in production.

## Running

From the `Notification-Service` directory:

```bash
mvn spring-boot:run
```

End-to-end test:

1. Start `kafka` (`docker compose up -d kafka` from the repo root).
2. Start `Auth-Service`, `Booking-Service`, and `Notification-Service`.
3. Register / log in via the API gateway to get a JWT.
4. Create a booking through `POST /booking/v1/bookings`. The booking row will now
   carry your email/name.
5. Confirm or cancel the booking through `POST /booking/v1/bookings/{id}/confirm` or
   `DELETE /booking/v1/bookings/{id}`. Within a few seconds you should see the
   email arrive in the inbox of the email tied to the user account.

If something goes wrong, check:

- `notification_log` in Postgres — every attempt is recorded with status `SENT`/`FAILED` and the error message.
- The `booking.notifications.dlt` Kafka topic — receives records that exhausted all retries.

## Configuration reference

All knobs (with defaults) in [src/main/resources/application.yaml](src/main/resources/application.yaml):

| Property | Default | Purpose |
|---|---|---|
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka brokers |
| `spring.mail.host` / `port` | `smtp.gmail.com` / `587` | SMTP relay |
| `MAIL_USERNAME` (env) | _(required)_ | Gmail address used for auth and `From:` |
| `MAIL_APP_PASSWORD` (env) | _(required)_ | Gmail 16-char app password |
| `notification.from-email` | `${MAIL_USERNAME}` | Email `From:` address |
| `notification.from-name` | `BookMyShow` | Email `From:` display name |
| `notification.topics.booking-confirmed` | `booking.confirmed` | Confirmation topic |
| `notification.topics.booking-cancelled` | `booking.cancelled` | Cancellation topic |
| `notification.topics.dead-letter` | `booking.notifications.dlt` | DLT for failed sends |
| `notification.retry.max-attempts` | `3` | Total delivery attempts (initial + retries) |
| `notification.retry.initial-interval-ms` | `2000` | Back-off start (ms) |
| `notification.retry.multiplier` | `2.0` | Back-off multiplier |
