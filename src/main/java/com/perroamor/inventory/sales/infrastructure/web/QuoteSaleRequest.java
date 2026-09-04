package com.perroamor.inventory.sales.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record QuoteSaleRequest(
        @NotEmpty @Valid List<Item> items,
        Boolean isWholesale
) {

    public record Item(
            @NotNull Long productId,
            @NotNull @Positive Integer quantity,
            @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal unitPrice,
            @Size(max = 255) String personalization
    ) {
    }
}
