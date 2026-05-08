package com.perroamor.inventory.sales.infrastructure.web;

import com.perroamor.inventory.sales.domain.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record SaleStatsResponse(
        Long eventId,
        long totalSales,
        BigDecimal totalAmount,
        List<PaymentMethodBreakdown> byPaymentMethod
) {
    public record PaymentMethodBreakdown(
            PaymentMethod paymentMethod,
            long count,
            BigDecimal amount
    ) {
    }
}
