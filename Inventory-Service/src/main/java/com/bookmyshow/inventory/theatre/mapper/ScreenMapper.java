package com.bookmyshow.inventory.theatre.mapper;

import com.bookmyshow.inventory.theatre.entity.Screen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScreenMapper {

    @Mapping(source = "theatre.id", target = "theatreId")
    @Mapping(source = "format.id", target = "formatId")
    @Mapping(source = "format.name", target = "formatName")
    com.bookmyshow.inventory.model.Screen toDto(Screen entity);
}
