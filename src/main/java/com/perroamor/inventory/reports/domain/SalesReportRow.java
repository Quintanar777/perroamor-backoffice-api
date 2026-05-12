package com.perroamor.inventory.reports.domain;

import java.math.BigDecimal;

public record SalesReportRow(
        Long brandId,
        String brandName,
        Long productId,
        String productName,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount
) {}
