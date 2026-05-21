package com.bookmyshow.auth.exception;

/**
 * Thrown when a request cannot be completed because of the current state of
 * the target resource — e.g., assigning a role the user already has. Maps to
 * HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
