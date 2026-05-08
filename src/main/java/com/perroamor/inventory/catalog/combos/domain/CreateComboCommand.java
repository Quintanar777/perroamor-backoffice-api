package com.perroamor.inventory.catalog.combos.domain;

import java.math.BigDecimal;
import java.util.List;

public record CreateComboCommand(
        String name,
        String description,
        Long brandId,
        BigDecimal price,
        BigDecimal wholesalePrice,
        List<NewItem> items
) {
    public record NewItem(
            Long productId,
            Long variantId,
            int quantity
    ) {
    }
}
