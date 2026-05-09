package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;

public record SaleItem(
        Long id,
        Long saleId,
        Long productId,
        String productName,
        Long variantId,
        String variantName,
        Long comboId,
        String comboName,
        int quantity,
        BigDecimal unitPrice,
        String personalization,
        BigDecimal lineTotal
) {
}
