package com.bookmyshow.inventory.movie.mapper;

import com.bookmyshow.inventory.movie.entity.MovieLanguageFormat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovieLanguageFormatMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.name", target = "languageName")
    @Mapping(source = "format.id", target = "formatId")
    @Mapping(source = "format.name", target = "formatName")
    com.bookmyshow.inventory.model.MovieLanguageFormat toDto(MovieLanguageFormat entity);
}
