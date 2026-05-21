package com.bookmyshow.inventory.exception;

/**
 * Thrown when a request cannot be completed because of the current state of the
 * target resource — e.g., duplicate creation, deletion of a resource still
 * referenced by other entities. Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
