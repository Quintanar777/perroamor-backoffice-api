package com.perroamor.inventory.catalog.infrastructure.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(min = 2, max = 150) String name,
        @NotNull Long brandId,
        @NotBlank @Size(min = 2, max = 80) String category,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal wholesalePrice,
        @PositiveOrZero Integer stock,
        @Size(max = 1000) String description,
        Boolean canBePersonalized,
        Boolean hasVariants,
        Boolean isActive
) {
}
