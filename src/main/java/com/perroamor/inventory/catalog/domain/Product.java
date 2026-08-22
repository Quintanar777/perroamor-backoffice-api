package com.perroamor.inventory.catalog.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(
        Long id,
        String name,
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
        return new Product(id, name, code, brandId, brandName, brandColor, category, price,
                wholesalePrice, newStock, description, canBePersonalized, hasVariants,
                isActive, createdAt, updatedAt);
    }
}
