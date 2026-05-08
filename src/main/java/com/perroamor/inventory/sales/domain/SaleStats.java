package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;
import java.util.List;

public record SaleStats(
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
