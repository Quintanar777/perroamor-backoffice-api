package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DiscountSlotOptionRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal finalUnitPrice
) {
}
