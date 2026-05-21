package com.bookmyshow.inventory.common.repository;

import com.bookmyshow.inventory.common.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    boolean existsByNameIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCase(
            String name, String state, String country);
}
