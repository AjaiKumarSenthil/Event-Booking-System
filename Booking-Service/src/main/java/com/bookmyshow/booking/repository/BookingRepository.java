package com.bookmyshow.booking.repository;

import com.bookmyshow.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.seats WHERE b.userId = :userId ORDER BY b.id DESC")
    List<Booking> findByUserIdOrderByCreatedDesc(UUID userId);
}
