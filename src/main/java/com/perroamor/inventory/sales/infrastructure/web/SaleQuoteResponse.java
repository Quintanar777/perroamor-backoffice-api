package com.perroamor.inventory.sales.infrastructure.web;

import java.math.BigDecimal;
import java.util.List;

public record SaleQuoteResponse(
        BigDecimal itemsTotal,
        Long discountId,
        String discountName,
        List<Item> items
) {

    public record Item(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            Long discountId,
            String discountName,
            String personalization,
            BigDecimal lineTotal
    ) {
    }
}
