package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import java.math.BigDecimal;

public record DiscountSlotOptionResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal finalUnitPrice
) {
}
