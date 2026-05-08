package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "isActive", source = "active")
    Brand toDomain(BrandJpaEntity entity);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "createdAt", ignore = true)
    BrandJpaEntity toEntity(Brand domain);
}
