package com.perroamor.inventory.reports.domain;

import java.math.BigDecimal;
import java.util.List;

public record SalesReportRow(
        Long brandId,
        String brandName,
        Long productId,
        String productName,
        int currentStock,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount,
        List<SalesReportVariantRow> variants
) {}
