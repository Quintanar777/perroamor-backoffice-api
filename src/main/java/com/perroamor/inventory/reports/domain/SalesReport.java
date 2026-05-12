package com.perroamor.inventory.reports.domain;

import java.util.List;

public record SalesReport(
        SalesReportSummary summary,
        List<SalesReportRow> rows
) {}
