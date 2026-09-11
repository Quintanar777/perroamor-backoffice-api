package com.perroamor.inventory.catalog.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(
        Long id,
        String name,
        String size,
        String code,
        Long brandId,
        String brandName,
        String brandColor,
        String category,
        BigDecimal price,
        BigDecimal wholesalePrice,
        int stock,
        String description,
        boolean canBePersonalized,
        boolean hasVariants,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Product withStock(int newStock) {
        return new Product(id, name, size, code, brandId, brandName, brandColor, category, price,
                wholesalePrice, newStock, description, canBePersonalized, hasVariants,
                isActive, createdAt, updatedAt);
    }

    public Product withCode(String newCode) {
        return new Product(id, name, size, newCode, brandId, brandName, brandColor, category, price,
                wholesalePrice, stock, description, canBePersonalized, hasVariants,
                isActive, createdAt, updatedAt);
    }
}
