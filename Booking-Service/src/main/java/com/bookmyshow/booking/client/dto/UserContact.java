package com.bookmyshow.booking.client.dto;

import java.util.UUID;

public record UserContact(UUID id, String email, String fullName) {
}
