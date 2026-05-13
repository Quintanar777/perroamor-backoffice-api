package com.perroamor.inventory.reports.infrastructure.web;

import java.math.BigDecimal;

public record SalesReportVariantRowResponse(
        Long variantId,
        String variantName,
        int currentStock,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount
) {}
