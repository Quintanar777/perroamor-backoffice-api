package com.perroamor.inventory.reports.domain;

import java.math.BigDecimal;

public record SalesReportVariantRow(
        Long productId,
        Long variantId,
        String variantName,
        int currentStock,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount
) {}
