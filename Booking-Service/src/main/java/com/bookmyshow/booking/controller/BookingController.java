package com.bookmyshow.booking.controller;

import com.bookmyshow.booking.api.BookingsApi;
import com.bookmyshow.booking.api.InternalApi;
import com.bookmyshow.booking.model.BookingListResponse;
import com.bookmyshow.booking.model.BookingRequest;
import com.bookmyshow.booking.model.BookingResponse;
import com.bookmyshow.booking.model.PaymentConfirmation;
import com.bookmyshow.booking.service.IBookingService;
import com.bookmyshow.jwt.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BookingController implements BookingsApi, InternalApi {

    private final IBookingService bookingService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<BookingResponse> createBooking(BookingRequest bookingRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(bookingRequest));
    }

    @Override
    public ResponseEntity<BookingResponse> getBookingDetails(UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
    }

    @Override
    public ResponseEntity<BookingListResponse> getBookings() {
        UUID currentUserId = AuthContext.getUserId();
        return ResponseEntity.ok(bookingService.getBookings(currentUserId));
    }

    @Override
    public ResponseEntity<BookingResponse> cancelBooking(UUID bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @Override
    public ResponseEntity<BookingResponse> confirmBooking(UUID bookingId,
                                                           PaymentConfirmation paymentConfirmation) {
        // TODO(security): once internal-service auth lands (see REMEDIATION_PLAN.md item #2),
        // allow the payment service to call this without the user-ownership check.
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId, paymentConfirmation));
    }
}
