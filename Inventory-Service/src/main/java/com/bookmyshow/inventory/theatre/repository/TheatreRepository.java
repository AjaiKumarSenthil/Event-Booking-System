package com.bookmyshow.inventory.theatre.repository;

import com.bookmyshow.inventory.theatre.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, UUID> {

    @Query("SELECT t FROM Theatre t " +
            "JOIN FETCH t.city " +
            "WHERE t.city.id = :cityId")
    List<Theatre> findByCityId(Integer cityId);

    boolean existsByCity_Id(Integer cityId);

    @Query("SELECT t FROM Theatre t " +
            "JOIN FETCH t.city " +
            "WHERE t.id = :id")
    java.util.Optional<Theatre> findByIdWithCity(UUID id);
}
