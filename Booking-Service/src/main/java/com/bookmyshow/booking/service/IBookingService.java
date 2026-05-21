package com.bookmyshow.booking.service;

import com.bookmyshow.booking.model.BookingListResponse;
import com.bookmyshow.booking.model.BookingRequest;
import com.bookmyshow.booking.model.BookingResponse;
import com.bookmyshow.booking.model.PaymentConfirmation;

import java.util.UUID;

public interface IBookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingDetails(UUID bookingId);

    BookingListResponse getBookings(UUID userId);

    BookingResponse cancelBooking(UUID bookingId);

    BookingResponse confirmBooking(UUID bookingId, PaymentConfirmation paymentConfirmation);
}
