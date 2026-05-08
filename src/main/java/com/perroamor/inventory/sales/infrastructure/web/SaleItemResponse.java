package com.perroamor.inventory.sales.infrastructure.web;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        Long productId,
        Long variantId,
        int quantity,
        BigDecimal unitPrice,
        String personalization,
        BigDecimal lineTotal
) {
}
