package com.bookmyshow.inventory.common.service;

import com.bookmyshow.inventory.model.City;
import com.bookmyshow.inventory.model.CityCreateRequest;
import com.bookmyshow.inventory.model.CityListResponse;
import com.bookmyshow.inventory.model.CityUpdateRequest;

public interface ICityService {

    CityListResponse findAllCities();

    City createCity(CityCreateRequest request);

    City updateCity(Integer id, CityUpdateRequest request);

    void deleteCity(Integer id);
}
