package com.perroamor.inventory.reports.infrastructure.web;

import java.math.BigDecimal;
import java.util.List;

public record SalesReportRowResponse(
        Long brandId,
        String brandName,
        Long productId,
        String productName,
        int currentStock,
        long totalQuantity,
        BigDecimal totalRevenue,
        long salesCount,
        List<SalesReportVariantRowResponse> variants
) {}
