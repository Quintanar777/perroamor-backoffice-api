package com.perroamor.inventory.catalog.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductVariant(
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
    public BigDecimal effectivePrice(BigDecimal productPrice) {
        return productPrice.add(priceAdjustment == null ? BigDecimal.ZERO : priceAdjustment);
    }
}
