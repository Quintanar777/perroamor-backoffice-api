package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record DiscountRequest(
        @NotBlank @Size(min = 2, max = 150) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal totalPrice,
        Boolean isActive,
        @NotEmpty @Valid List<DiscountSlotRequest> slots
) {
}
