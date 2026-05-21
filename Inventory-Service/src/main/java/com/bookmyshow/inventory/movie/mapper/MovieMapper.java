package com.bookmyshow.inventory.movie.mapper;

import com.bookmyshow.inventory.model.MovieCreateRequest;
import com.bookmyshow.inventory.model.MovieDetails;
import com.bookmyshow.inventory.model.MovieUpdateRequest;
import com.bookmyshow.inventory.movie.entity.Movie;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(source = "durationMin", target = "duration")
    com.bookmyshow.inventory.model.Movie toDto(Movie movie);

    @Mapping(source = "durationMin", target = "duration")
    MovieDetails toDetailsDto(Movie movie);

    @Mapping(target = "id", ignore = true)
    Movie toEntity(MovieCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Movie entity, MovieUpdateRequest request);
}
