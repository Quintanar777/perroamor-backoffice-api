package com.perroamor.inventory.reports.infrastructure.web;

import com.perroamor.inventory.reports.application.SalesReportService;
import com.perroamor.inventory.reports.domain.SalesReport;
import com.perroamor.inventory.reports.domain.SalesReportFilter;
import com.perroamor.inventory.reports.domain.SalesReportRow;
import com.perroamor.inventory.sales.domain.PaymentMethod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports/sales")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class SalesReportController {

    private final SalesReportService service;

    public SalesReportController(SalesReportService service) {
        this.service = service;
    }

    @GetMapping
    public SalesReportResponse query(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReport report = service.query(new SalesReportFilter(brandId, productId, paymentMethod, startDate, endDate));
        return toResponse(report);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReport report = service.query(new SalesReportFilter(brandId, productId, paymentMethod, startDate, endDate));
        byte[] csv = buildCsv(report.rows());

        String filename = "reporte-ventas-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    private SalesReportResponse toResponse(SalesReport report) {
        var summary = new SalesReportSummaryResponse(
                report.summary().totalSalesCount(),
                report.summary().totalQuantity(),
                report.summary().totalRevenue());
        var rows = report.rows().stream()
                .map(r -> new SalesReportRowResponse(
                        r.brandId(), r.brandName(),
                        r.productId(), r.productName(), r.currentStock(),
                        r.totalQuantity(), r.totalRevenue(), r.salesCount(),
                        r.variants().stream()
                                .map(v -> new SalesReportVariantRowResponse(
                                        v.variantId(), v.variantName(), v.currentStock(),
                                        v.totalQuantity(), v.totalRevenue(), v.salesCount()))
                                .toList()))
                .toList();
        return new SalesReportResponse(summary, rows);
    }

    private byte[] buildCsv(List<SalesReportRow> rows) {
        var sb = new StringBuilder();
        sb.append('﻿'); // BOM UTF-8 para compatibilidad con Excel
        sb.append("Marca,Producto,Variante,Stock Actual,Unidades Vendidas,Ingresos,Num. Ventas\n");
        for (SalesReportRow row : rows) {
            sb.append(escapeCsv(row.brandName())).append(',');
            sb.append(escapeCsv(row.productName())).append(',');
            sb.append(','); // sin variante
            sb.append(row.currentStock()).append(',');
            sb.append(row.totalQuantity()).append(',');
            sb.append(row.totalRevenue()).append(',');
            sb.append(row.salesCount()).append('\n');
            for (var v : row.variants()) {
                sb.append(','); // sin marca
                sb.append(escapeCsv(row.productName())).append(',');
                sb.append(escapeCsv(v.variantName())).append(',');
                sb.append(v.currentStock()).append(',');
                sb.append(v.totalQuantity()).append(',');
                sb.append(v.totalRevenue()).append(',');
                sb.append(v.salesCount()).append('\n');
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
