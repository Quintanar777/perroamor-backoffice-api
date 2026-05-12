package com.perroamor.inventory.reports.domain;

import java.math.BigDecimal;

public record SalesReportSummary(
        long totalSalesCount,
        long totalQuantity,
        BigDecimal totalRevenue
) {}
