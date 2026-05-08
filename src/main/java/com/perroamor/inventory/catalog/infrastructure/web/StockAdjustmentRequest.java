package com.perroamor.inventory.catalog.infrastructure.web;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull Integer delta,
        Integer setTo
) {
}
