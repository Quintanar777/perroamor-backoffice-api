package com.perroamor.inventory.sales.infrastructure.web;

import com.perroamor.inventory.sales.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateSaleRequest(
        @NotNull Long eventId,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 150) String customerName,
        @Size(max = 40)  String customerPhone,
        @Size(max = 150) String customerEmail,
        @Size(max = 500) String notes,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal discountAmount,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal taxAmount,
        Boolean isPaid,
        @NotEmpty @Valid List<Item> items
) {

    public record Item(
            @NotNull Long productId,
            Long variantId,
            @NotNull @Positive Integer quantity,
            @Size(max = 255) String personalization
    ) {
    }
}
