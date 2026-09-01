package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;
import java.util.List;

public record UpdateDiscountCommand(
        String name,
        String description,
        BigDecimal totalPrice,
        boolean isActive,
        List<CreateDiscountCommand.NewSlot> slots
) {
}
