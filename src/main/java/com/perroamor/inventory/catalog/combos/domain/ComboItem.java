package com.perroamor.inventory.catalog.combos.domain;

public record ComboItem(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantName,
        int quantity
) {
}
