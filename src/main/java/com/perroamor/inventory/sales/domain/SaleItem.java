package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;

public record SaleItem(
        Long id,
        Long saleId,
        Long productId,
        Long variantId,
        Long comboId,
        String comboName,
        int quantity,
        BigDecimal unitPrice,
        String personalization,
        BigDecimal lineTotal
) {
}
