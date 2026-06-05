package com.perroamor.inventory.reports.infrastructure.persistence;

import com.perroamor.inventory.reports.domain.SalesReport;
import com.perroamor.inventory.reports.domain.SalesReportFilter;
import com.perroamor.inventory.reports.domain.SalesReportLine;
import com.perroamor.inventory.reports.domain.SalesReportRepository;
import com.perroamor.inventory.reports.domain.SalesReportRow;
import com.perroamor.inventory.reports.domain.SalesReportSummary;
import com.perroamor.inventory.reports.domain.SalesReportVariantRow;
import com.perroamor.inventory.sales.domain.PaymentMethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SalesReportRepositoryAdapter implements SalesReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public SalesReport query(SalesReportFilter filter) {
        Map<Long, List<SalesReportVariantRow>> variantsByProduct = queryVariantRows(filter)
                .stream()
                .collect(Collectors.groupingBy(SalesReportVariantRow::productId));

        List<SalesReportRow> rows = queryProductRows(filter).stream()
                .map(r -> new SalesReportRow(
                        r.brandId(), r.brandName(),
                        r.productId(), r.productName(), r.currentStock(),
                        r.totalQuantity(), r.totalRevenue(), r.salesCount(),
                        variantsByProduct.getOrDefault(r.productId(), List.of())))
                .toList();

        return new SalesReport(buildSummary(rows, filter), rows);
    }

    @Override
    public List<SalesReportLine> queryLines(SalesReportFilter filter) {
        // LEFT JOIN explícito en variant: es nullable. Un path implícito (si.variant.x)
        // haría INNER JOIN y descartaría las líneas de productos sin variante.
        var jpql = new StringBuilder("""
                SELECT si.sale.id, si.sale.saleDate, si.sale.event.name, si.sale.paymentMethod,
                       si.product.brand.name, si.product.name, v.variantName,
                       si.quantity, si.unitPrice, si.lineTotal,
                       si.product.price, v.priceAdjustment
                FROM SaleItemJpaEntity si
                LEFT JOIN si.variant v
                WHERE si.product IS NOT NULL
                AND si.sale.isCancelled = false
                """);

        appendConditions(jpql, filter);

        jpql.append("""
                ORDER BY si.sale.id ASC, si.product.brand.name ASC, si.product.name ASC
                """);

        var query = em.createQuery(jpql.toString());
        bindParameters(query, filter);

        @SuppressWarnings("unchecked")
        List<Object[]> raw = query.getResultList();

        return raw.stream().map(r -> {
            BigDecimal productPrice = (BigDecimal) r[10];
            BigDecimal priceAdjustment = (BigDecimal) r[11];
            BigDecimal currentPrice = productPrice == null
                    ? null
                    : productPrice.add(priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO);

            return new SalesReportLine(
                    (Long) r[0], (LocalDateTime) r[1], (String) r[2], (PaymentMethod) r[3],
                    (String) r[4], (String) r[5], (String) r[6],
                    ((Number) r[7]).longValue(), (BigDecimal) r[8], currentPrice, (BigDecimal) r[9]
            );
        }).toList();
    }

    private List<SalesReportRow> queryProductRows(SalesReportFilter filter) {
        var jpql = new StringBuilder("""
                SELECT si.product.brand.id, si.product.brand.name,
                       si.product.id, si.product.name, si.product.stock,
                       SUM(si.quantity), SUM(si.lineTotal), COUNT(DISTINCT si.sale.id)
                FROM SaleItemJpaEntity si
                WHERE si.product IS NOT NULL
                AND si.sale.isCancelled = false
                """);

        appendConditions(jpql, filter);

        jpql.append("""
                GROUP BY si.product.brand.id, si.product.brand.name,
                         si.product.id, si.product.name, si.product.stock
                ORDER BY SUM(si.lineTotal) DESC
                """);

        var query = em.createQuery(jpql.toString());
        bindParameters(query, filter);

        @SuppressWarnings("unchecked")
        List<Object[]> raw = query.getResultList();

        return raw.stream().map(r -> new SalesReportRow(
                (Long) r[0], (String) r[1],
                (Long) r[2], (String) r[3], ((Number) r[4]).intValue(),
                ((Number) r[5]).longValue(), (BigDecimal) r[6], (Long) r[7],
                List.of()
        )).toList();
    }

    private List<SalesReportVariantRow> queryVariantRows(SalesReportFilter filter) {
        var jpql = new StringBuilder("""
                SELECT si.product.id,
                       si.variant.id, si.variant.variantName, si.variant.stock,
                       SUM(si.quantity), SUM(si.lineTotal), COUNT(DISTINCT si.sale.id)
                FROM SaleItemJpaEntity si
                WHERE si.product IS NOT NULL
                AND si.variant IS NOT NULL
                AND si.sale.isCancelled = false
                """);

        appendConditions(jpql, filter);

        jpql.append("""
                GROUP BY si.product.id,
                         si.variant.id, si.variant.variantName, si.variant.stock
                ORDER BY si.product.id ASC, SUM(si.lineTotal) DESC
                """);

        var query = em.createQuery(jpql.toString());
        bindParameters(query, filter);

        @SuppressWarnings("unchecked")
        List<Object[]> raw = query.getResultList();

        return raw.stream().map(r -> new SalesReportVariantRow(
                (Long) r[0],
                (Long) r[1], (String) r[2], ((Number) r[3]).intValue(),
                ((Number) r[4]).longValue(), (BigDecimal) r[5], (Long) r[6]
        )).toList();
    }

    private void appendConditions(StringBuilder jpql, SalesReportFilter filter) {
        if (filter.eventId() != null)       jpql.append("AND si.sale.event.id = :eventId\n");
        if (filter.brandId() != null)       jpql.append("AND si.product.brand.id = :brandId\n");
        if (filter.productId() != null)     jpql.append("AND si.product.id = :productId\n");
        if (filter.paymentMethod() != null) jpql.append("AND si.sale.paymentMethod = :paymentMethod\n");
        if (filter.startDate() != null)     jpql.append("AND si.sale.saleDate >= :startDate\n");
        if (filter.endDate() != null)       jpql.append("AND si.sale.saleDate <= :endDate\n");
    }

    private void bindParameters(jakarta.persistence.Query query, SalesReportFilter filter) {
        if (filter.eventId() != null)       query.setParameter("eventId", filter.eventId());
        if (filter.brandId() != null)       query.setParameter("brandId", filter.brandId());
        if (filter.productId() != null)     query.setParameter("productId", filter.productId());
        if (filter.paymentMethod() != null) query.setParameter("paymentMethod", filter.paymentMethod());
        if (filter.startDate() != null)     query.setParameter("startDate", filter.startDate());
        if (filter.endDate() != null)       query.setParameter("endDate", filter.endDate());
    }

    private SalesReportSummary buildSummary(List<SalesReportRow> rows, SalesReportFilter filter) {
        long totalQuantity = rows.stream().mapToLong(SalesReportRow::totalQuantity).sum();

        var jpql = new StringBuilder("""
                SELECT COUNT(s.id), SUM(s.totalAmount)
                FROM SaleJpaEntity s
                WHERE s.isCancelled = false
                """);
        if (filter.eventId() != null)       jpql.append("AND s.event.id = :eventId\n");
        if (filter.paymentMethod() != null) jpql.append("AND s.paymentMethod = :paymentMethod\n");
        if (filter.startDate() != null)     jpql.append("AND s.saleDate >= :startDate\n");
        if (filter.endDate() != null)       jpql.append("AND s.saleDate <= :endDate\n");

        var query = em.createQuery(jpql.toString());
        if (filter.eventId() != null)       query.setParameter("eventId", filter.eventId());
        if (filter.paymentMethod() != null) query.setParameter("paymentMethod", filter.paymentMethod());
        if (filter.startDate() != null)     query.setParameter("startDate", filter.startDate());
        if (filter.endDate() != null)       query.setParameter("endDate", filter.endDate());

        Object[] result = (Object[]) query.getSingleResult();
        long totalSalesCount = ((Number) result[0]).longValue();
        BigDecimal totalRevenue = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;

        return new SalesReportSummary(totalSalesCount, totalQuantity, totalRevenue);
    }
}
