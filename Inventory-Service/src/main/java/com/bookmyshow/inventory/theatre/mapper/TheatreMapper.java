package com.bookmyshow.inventory.theatre.mapper;

import com.bookmyshow.inventory.theatre.entity.Theatre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TheatreMapper {

    @Mapping(source = "address", target = "location")
    @Mapping(source = "city.name", target = "city")
    @Mapping(source = "city.id", target = "cityId")
    com.bookmyshow.inventory.model.Theatre toDto(Theatre theatre);
}
