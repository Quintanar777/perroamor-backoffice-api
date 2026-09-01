package com.perroamor.inventory.catalog.discounts.domain;

public record DiscountFilter(
        Boolean isActive,
        String query
) {
}
