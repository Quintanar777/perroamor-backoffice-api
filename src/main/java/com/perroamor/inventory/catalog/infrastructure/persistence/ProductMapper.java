package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "brandColor", source = "brand.baseColor")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "withStock", ignore = true)
    Product toDomain(ProductJpaEntity entity);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductJpaEntity toEntity(Product domain);
}
