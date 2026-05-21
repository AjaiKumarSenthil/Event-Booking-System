package com.bookmyshow.booking.exception;

public class BookingNotAllowedException extends RuntimeException {

    public BookingNotAllowedException(String message) {
        super(message);
    }
}
