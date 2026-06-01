package com.perroamor.inventory.reports.domain;

import java.util.List;

public interface SalesReportRepository {
    SalesReport query(SalesReportFilter filter);

    List<SalesReportLine> queryLines(SalesReportFilter filter);
}
