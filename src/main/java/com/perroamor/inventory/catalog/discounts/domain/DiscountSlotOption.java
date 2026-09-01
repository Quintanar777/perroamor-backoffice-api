package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;

public record DiscountSlotOption(
        Long id,
        Long productId,
        String productName,
        BigDecimal finalUnitPrice
) {
}
