package com.bookmyshow.inventory.common.mapper;

import com.bookmyshow.inventory.common.entity.Format;
import com.bookmyshow.inventory.model.FormatCreateRequest;
import com.bookmyshow.inventory.model.FormatUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FormatMapper {

    com.bookmyshow.inventory.model.Format toDto(Format entity);

    @Mapping(target = "id", ignore = true)
    Format toEntity(FormatCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Format entity, FormatUpdateRequest request);
}
