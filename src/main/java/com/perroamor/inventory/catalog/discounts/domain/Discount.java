package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Discount(
        Long id,
        String name,
        String description,
        BigDecimal totalPrice,
        boolean isActive,
        LocalDateTime createdAt,
        List<DiscountSlot> slots
) {
}
