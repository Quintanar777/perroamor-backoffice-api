package com.perroamor.inventory.catalog.discounts.domain;

import java.util.List;

public record DiscountSlot(
        Long id,
        int position,
        SlotType slotType,
        int quantity,
        List<DiscountSlotOption> options
) {
}
