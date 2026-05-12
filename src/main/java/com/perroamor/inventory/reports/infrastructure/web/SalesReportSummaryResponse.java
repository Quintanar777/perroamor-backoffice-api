package com.perroamor.inventory.reports.infrastructure.web;

import java.math.BigDecimal;

public record SalesReportSummaryResponse(
        long totalSalesCount,
        long totalQuantity,
        BigDecimal totalRevenue
) {}
