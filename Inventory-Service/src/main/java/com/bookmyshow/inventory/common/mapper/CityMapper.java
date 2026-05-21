package com.bookmyshow.inventory.common.mapper;

import com.bookmyshow.inventory.common.entity.City;
import com.bookmyshow.inventory.model.CityCreateRequest;
import com.bookmyshow.inventory.model.CityUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CityMapper {

    com.bookmyshow.inventory.model.City toDto(City entity);

    @Mapping(target = "id", ignore = true)
    City toEntity(CityCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget City entity, CityUpdateRequest request);

    default BigDecimal mapDoubleToBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    default Double mapBigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
