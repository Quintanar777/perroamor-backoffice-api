package com.perroamor.inventory.catalog.combos.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ComboRequest(
        @NotBlank @Size(min = 2, max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull Long brandId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal wholesalePrice,
        Boolean isActive,
        @NotEmpty @Valid List<ComboItemRequest> items
) {
}
