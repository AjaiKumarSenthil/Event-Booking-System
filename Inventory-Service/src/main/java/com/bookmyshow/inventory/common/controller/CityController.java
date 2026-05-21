package com.bookmyshow.inventory.common.controller;

import com.bookmyshow.inventory.api.CitiesApi;
import com.bookmyshow.inventory.common.service.ICityService;
import com.bookmyshow.inventory.model.City;
import com.bookmyshow.inventory.model.CityCreateRequest;
import com.bookmyshow.inventory.model.CityListResponse;
import com.bookmyshow.inventory.model.CityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CityController implements CitiesApi {

    private final ICityService cityService;

    @Override
    public ResponseEntity<CityListResponse> getCities() {
        return ResponseEntity.ok(cityService.findAllCities());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<City> createCity(CityCreateRequest cityCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cityService.createCity(cityCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<City> updateCity(Integer id, CityUpdateRequest cityUpdateRequest) {
        return ResponseEntity.ok(cityService.updateCity(id, cityUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCity(Integer id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}
