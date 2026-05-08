package com.perroamor.inventory.catalog.combos.infrastructure.web;

public record ComboItemResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantName,
        int quantity
) {
}
