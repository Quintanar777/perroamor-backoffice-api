package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;

public record SaleItem(
        Long id,
        Long saleId,
        Long productId,
        Long variantId,
        int quantity,
        BigDecimal unitPrice,
        String personalization,
        BigDecimal lineTotal
) {
}
