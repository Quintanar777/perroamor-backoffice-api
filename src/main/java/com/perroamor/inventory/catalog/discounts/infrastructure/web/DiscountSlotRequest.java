package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import com.perroamor.inventory.catalog.discounts.domain.SlotType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DiscountSlotRequest(
        @NotNull @Min(0) Integer position,
        @NotNull SlotType slotType,
        @NotNull @Positive Integer quantity,
        @NotEmpty @Valid List<DiscountSlotOptionRequest> options
) {
}
