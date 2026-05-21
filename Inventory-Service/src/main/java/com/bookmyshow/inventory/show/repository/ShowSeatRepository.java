package com.bookmyshow.inventory.show.repository;

import com.bookmyshow.inventory.common.enums.SeatStatus;
import com.bookmyshow.inventory.show.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "WHERE ss.show.id = :showId " +
            "ORDER BY s.rowLabel, s.seatNumber")
    List<ShowSeat> findAllByShowIdWithSeat(UUID showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "WHERE ss.show.id = :showId " +
            "AND CONCAT(s.rowLabel, '-', s.seatNumber) IN :seatKeys " +
            "ORDER BY s.rowLabel, s.seatNumber")
    List<ShowSeat> findByShowIdAndSeatsForUpdate(UUID showId, List<String> seatKeys);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :status WHERE ss.id IN :ids")
    void updateStatusByIds(List<UUID> ids, SeatStatus status);

    boolean existsByShow_IdAndStatus(UUID showId, SeatStatus status);

    @Modifying
    @Query("DELETE FROM ShowSeat ss WHERE ss.show.id = :showId")
    void deleteAllByShowId(UUID showId);
}
