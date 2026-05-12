package com.perroamor.inventory.reports.domain;

public interface SalesReportRepository {
    SalesReport query(SalesReportFilter filter);
}
