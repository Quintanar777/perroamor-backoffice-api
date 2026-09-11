package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DiscountResponse(
        Long id,
        String name,
        String description,
        BigDecimal totalPrice,
        boolean isActive,
        LocalDateTime createdAt,
        List<DiscountSlotResponse> slots
) {
}
