package com.perroamor.inventory.catalog.domain;

public record ProductFilter(
        Long brandId,
        String category,
        String size,
        String query,
        Boolean isActive
) {
}
