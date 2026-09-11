package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Sale(
        Long id,
        Long eventId,
        Long soldByUserId,
        String soldByUsername,
        LocalDateTime saleDate,
        PaymentMethod paymentMethod,
        String customerName,
        String customerPhone,
        String customerEmail,
        String notes,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        boolean isPaid,
        boolean isWholesale,
        boolean isCancelled,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        List<SaleItem> items
) {

    public BigDecimal subtotal() {
        return totalAmount.subtract(taxAmount).add(discountAmount);
    }
}
