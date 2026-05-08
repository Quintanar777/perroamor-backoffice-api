package com.perroamor.inventory.catalog.combos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Combo(
        Long id,
        String name,
        String description,
        Long brandId,
        String brandName,
        BigDecimal price,
        BigDecimal wholesalePrice,
        boolean isActive,
        LocalDateTime createdAt,
        List<ComboItem> items
) {
}
