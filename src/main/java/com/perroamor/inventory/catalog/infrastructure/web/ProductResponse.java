package com.perroamor.inventory.catalog.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
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
}
