package com.bookmyshow.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "booking_seat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "show_seat_id"}),
        indexes = @Index(name = "idx_booking_seat_booking", columnList = "booking_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "show_seat_id", nullable = false)
    private UUID showSeatId;

    @Column(nullable = false, length = 2)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatNumber;
}
