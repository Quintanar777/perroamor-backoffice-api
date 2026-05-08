package com.perroamor.inventory.catalog.infrastructure.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductVariantRequest(
        @NotBlank @Size(min = 2, max = 150) String variantName,
        @Size(max = 60) String color,
        @Size(max = 40) String size,
        @Size(max = 120) String design,
        @Size(max = 120) String material,
        @NotBlank @Size(min = 2, max = 60) String sku,
        @PositiveOrZero Integer stock,
        @DecimalMin("-9999.99") @Digits(integer = 8, fraction = 2) BigDecimal priceAdjustment,
        Boolean isActive
) {
}
