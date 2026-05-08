package com.perroamor.inventory.catalog.combos.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ComboItemRequest(
        @NotNull Long productId,
        Long variantId,
        @NotNull @Positive Integer quantity
) {
}
