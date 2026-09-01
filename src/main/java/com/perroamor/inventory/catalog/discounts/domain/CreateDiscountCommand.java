package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;
import java.util.List;

public record CreateDiscountCommand(
        String name,
        String description,
        BigDecimal totalPrice,
        List<NewSlot> slots
) {
    public record NewSlot(
            int position,
            SlotType slotType,
            int quantity,
            List<NewOption> options
    ) {
    }

    public record NewOption(
            Long productId,
            BigDecimal finalUnitPrice
    ) {
    }
}
