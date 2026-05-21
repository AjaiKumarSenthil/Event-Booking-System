package com.bookmyshow.inventory.theatre.repository;

import com.bookmyshow.inventory.theatre.entity.Seat;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends CrudRepository<Seat, UUID> {

    List<Seat> findAllByScreenId(UUID screenId);

    boolean existsByScreen_Id(UUID screenId);

    long countByScreen_Id(UUID screenId);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.screen.id = :screenId")
    void deleteAllByScreenId(UUID screenId);
}
