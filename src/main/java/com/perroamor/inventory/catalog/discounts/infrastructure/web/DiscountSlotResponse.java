package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import com.perroamor.inventory.catalog.discounts.domain.SlotType;

import java.util.List;

public record DiscountSlotResponse(
        Long id,
        int position,
        SlotType slotType,
        int quantity,
        List<DiscountSlotOptionResponse> options
) {
}
