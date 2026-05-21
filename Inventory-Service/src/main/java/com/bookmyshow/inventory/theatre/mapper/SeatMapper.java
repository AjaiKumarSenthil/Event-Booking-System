package com.bookmyshow.inventory.theatre.mapper;

import com.bookmyshow.inventory.model.TheatreSeat;
import com.bookmyshow.inventory.theatre.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "screen.id", target = "screenId")
    TheatreSeat toDto(Seat entity);
}
