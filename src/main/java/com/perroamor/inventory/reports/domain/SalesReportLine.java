package com.perroamor.inventory.reports.domain;

import com.perroamor.inventory.sales.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una línea de detalle del reporte de ventas: una fila por SaleItem (no agregado).
 * El saleId se repite cuando una venta tiene varios productos.
 *
 * unitPrice es el precio congelado al momento de la venta; currentPrice es el
 * precio efectivo actual del producto/variante. La diferencia entre ambos explica
 * por qué precio_actual × unidades no cuadra con el ingreso registrado.
 */
public record SalesReportLine(
        Long saleId,
        LocalDateTime saleDate,
        String eventName,
        PaymentMethod paymentMethod,
        String brandName,
        String productName,
        String variantName,
        long quantity,
        BigDecimal unitPrice,
        BigDecimal currentPrice,
        BigDecimal lineTotal
) {}
