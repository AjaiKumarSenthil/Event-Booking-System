package com.bookmyshow.inventory.common.repository;

import com.bookmyshow.inventory.common.entity.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormatRepository extends JpaRepository<Format, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
