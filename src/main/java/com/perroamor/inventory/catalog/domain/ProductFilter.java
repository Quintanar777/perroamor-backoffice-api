package com.perroamor.inventory.catalog.domain;

public record ProductFilter(
        Long brandId,
        String category,
        String query,
        Boolean isActive
) {
}
