package com.perroamor.inventory.catalog.combos.domain;

public record ComboFilter(
        Long brandId,
        Boolean isActive,
        String query
) {
}
