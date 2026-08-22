package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.domain.Brand;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    BrandResponse toBrandResponse(Brand brand);

    ProductResponse toProductResponse(Product product);

    ProductVariantResponse toVariantResponse(ProductVariant variant);

    default Product toDomain(ProductRequest request) {
        return new Product(
                null,
                request.name(),
                request.code(),
                request.brandId(),
                null,
                null,
                request.category(),
                request.price(),
                request.wholesalePrice(),
                request.stock() == null ? 0 : request.stock(),
                request.description(),
                Boolean.TRUE.equals(request.canBePersonalized()),
                Boolean.TRUE.equals(request.hasVariants()),
                request.isActive() == null || request.isActive(),
                null,
                null);
    }

    default ProductVariant toDomain(ProductVariantRequest request) {
        return new ProductVariant(
                null,
                null,
                request.variantName(),
                request.color(),
                request.size(),
                request.design(),
                request.material(),
                request.sku(),
                request.stock() == null ? 0 : request.stock(),
                request.priceAdjustment() == null ? BigDecimal.ZERO : request.priceAdjustment(),
                request.isActive() == null || request.isActive(),
                null);
    }
}
