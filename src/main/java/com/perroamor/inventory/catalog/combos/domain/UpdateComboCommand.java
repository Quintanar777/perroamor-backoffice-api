package com.perroamor.inventory.catalog.combos.domain;

import java.math.BigDecimal;
import java.util.List;

public record UpdateComboCommand(
        String name,
        String description,
        Long brandId,
        BigDecimal price,
        BigDecimal wholesalePrice,
        boolean isActive,
        List<CreateComboCommand.NewItem> items
) {
}
