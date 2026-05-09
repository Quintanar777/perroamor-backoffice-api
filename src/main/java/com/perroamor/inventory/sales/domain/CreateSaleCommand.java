package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;
import java.util.List;

public record CreateSaleCommand(
        Long eventId,
        Long soldByUserId,
        PaymentMethod paymentMethod,
        String customerName,
        String customerPhone,
        String customerEmail,
        String notes,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        boolean isPaid,
        List<NewItem> items
) {
    public record NewItem(
            Long productId,
            Long variantId,
            Long comboId,
            int quantity,
            BigDecimal unitPriceOverride,
            String personalization
    ) {
    }
}
