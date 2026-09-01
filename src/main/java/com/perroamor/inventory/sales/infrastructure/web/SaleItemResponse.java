package com.perroamor.inventory.sales.infrastructure.web;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantName,
        Long comboId,
        String comboName,
        Long discountId,
        String discountName,
        int quantity,
        BigDecimal unitPrice,
        String personalization,
        BigDecimal lineTotal
) {
}
