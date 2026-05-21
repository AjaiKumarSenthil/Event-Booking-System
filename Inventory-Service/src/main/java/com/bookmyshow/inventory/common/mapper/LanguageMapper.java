package com.bookmyshow.inventory.common.mapper;

import com.bookmyshow.inventory.common.entity.Language;
import com.bookmyshow.inventory.model.LanguageCreateRequest;
import com.bookmyshow.inventory.model.LanguageUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    com.bookmyshow.inventory.model.Language toDto(Language entity);

    @Mapping(target = "id", ignore = true)
    Language toEntity(LanguageCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Language entity, LanguageUpdateRequest request);
}
