package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "isActive", source = "active")
    ProductVariant toDomain(ProductVariantJpaEntity entity);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductVariantJpaEntity toEntity(ProductVariant domain);
}
