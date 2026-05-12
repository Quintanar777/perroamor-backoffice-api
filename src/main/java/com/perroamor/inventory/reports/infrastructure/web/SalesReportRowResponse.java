package com.perroamor.inventory.reports.infrastructure.web;

import java.math.BigDecimal;

public record SalesReportRowResponse(
        Long brandId,
        String brandName,
        Long productId,
        String productName,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount
) {}
