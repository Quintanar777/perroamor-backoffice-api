package com.perroamor.inventory.catalog.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductVariantResponse(
        Long id,
        Long productId,
        String variantName,
        String color,
        String size,
        String design,
        String material,
        String sku,
        int stock,
        BigDecimal priceAdjustment,
        boolean isActive,
        LocalDateTime createdAt
) {
}
