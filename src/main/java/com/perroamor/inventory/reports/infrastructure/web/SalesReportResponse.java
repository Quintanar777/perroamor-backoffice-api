package com.perroamor.inventory.reports.infrastructure.web;

import java.util.List;

public record SalesReportResponse(
        SalesReportSummaryResponse summary,
        List<SalesReportRowResponse> rows
) {}
