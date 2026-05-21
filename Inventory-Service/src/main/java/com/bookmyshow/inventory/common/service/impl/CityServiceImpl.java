package com.bookmyshow.inventory.common.service.impl;

import com.bookmyshow.inventory.common.entity.City;
import com.bookmyshow.inventory.common.mapper.CityMapper;
import com.bookmyshow.inventory.common.repository.CityRepository;
import com.bookmyshow.inventory.common.service.ICityService;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.model.CityCreateRequest;
import com.bookmyshow.inventory.model.CityListResponse;
import com.bookmyshow.inventory.model.CityUpdateRequest;
import com.bookmyshow.inventory.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements ICityService {

    private final CityRepository cityRepository;
    private final TheatreRepository theatreRepository;
    private final CityMapper cityMapper;

    @Override
    @Transactional(readOnly = true)
    public CityListResponse findAllCities() {
        return new CityListResponse().cities(
                cityRepository.findAll()
                        .stream()
                        .map(cityMapper::toDto)
                        .toList()
        );
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.City createCity(CityCreateRequest request) {
        if (cityRepository.existsByNameIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCase(
                request.getName(), request.getState(), request.getCountry())) {
            throw new ConflictException("City already exists: "
                    + request.getName() + ", " + request.getState() + ", " + request.getCountry());
        }

        City entity = cityMapper.toEntity(request);
        City saved = cityRepository.save(entity);
        return cityMapper.toDto(saved);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.City updateCity(Integer id, CityUpdateRequest request) {
        City entity = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + id));

        cityMapper.updateEntity(entity, request);
        City saved = cityRepository.save(entity);
        return cityMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCity(Integer id) {
        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("City not found: " + id);
        }
        if (theatreRepository.existsByCity_Id(id)) {
            throw new ConflictException("City " + id + " still referenced by theatres");
        }
        cityRepository.deleteById(id);
    }
}
