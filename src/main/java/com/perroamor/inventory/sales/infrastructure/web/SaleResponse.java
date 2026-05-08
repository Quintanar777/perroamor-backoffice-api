package com.perroamor.inventory.sales.infrastructure.web;

import com.perroamor.inventory.sales.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
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
        BigDecimal subtotal,
        boolean isPaid,
        boolean isCancelled,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        List<SaleItemResponse> items
) {
}
