package com.perroamor.inventory.catalog.combos.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComboResponse(
        Long id,
        String name,
        String description,
        Long brandId,
        String brandName,
        BigDecimal price,
        BigDecimal wholesalePrice,
        boolean isActive,
        int availableStock,
        LocalDateTime createdAt,
        List<ComboItemResponse> items
) {
}
