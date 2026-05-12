package com.perroamor.inventory.reports.domain;

import com.perroamor.inventory.sales.domain.PaymentMethod;

import java.time.LocalDateTime;

public record SalesReportFilter(
        Long brandId,
        Long productId,
        PaymentMethod paymentMethod,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
