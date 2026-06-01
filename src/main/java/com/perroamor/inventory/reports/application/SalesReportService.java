package com.perroamor.inventory.reports.application;

import com.perroamor.inventory.reports.domain.SalesReport;
import com.perroamor.inventory.reports.domain.SalesReportFilter;
import com.perroamor.inventory.reports.domain.SalesReportLine;
import com.perroamor.inventory.reports.domain.SalesReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesReportService {

    private final SalesReportRepository repository;

    public SalesReportService(SalesReportRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SalesReport query(SalesReportFilter filter) {
        return repository.query(filter);
    }

    @Transactional(readOnly = true)
    public List<SalesReportLine> queryLines(SalesReportFilter filter) {
        return repository.queryLines(filter);
    }
}
