package com.perroamor.inventory.sales.infrastructure.persistence;

import com.perroamor.inventory.sales.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SaleJpaRepository extends
        JpaRepository<SaleJpaEntity, Long>,
        JpaSpecificationExecutor<SaleJpaEntity> {

    @Query("""
            SELECT s.paymentMethod AS paymentMethod, COUNT(s) AS count, COALESCE(SUM(s.totalAmount), 0) AS amount
            FROM SaleJpaEntity s
            WHERE s.event.id = :eventId AND s.isCancelled = false
            GROUP BY s.paymentMethod
            ORDER BY s.paymentMethod ASC
            """)
    List<PaymentMethodAggregation> aggregateByPaymentMethod(@Param("eventId") Long eventId);

    @Query("""
            SELECT COUNT(s)
            FROM SaleJpaEntity s
            WHERE s.event.id = :eventId AND s.isCancelled = false
            """)
    long countActiveByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount), 0)
            FROM SaleJpaEntity s
            WHERE s.event.id = :eventId AND s.isCancelled = false
            """)
    BigDecimal sumActiveByEventId(@Param("eventId") Long eventId);

    interface PaymentMethodAggregation {
        PaymentMethod getPaymentMethod();
        long getCount();
        BigDecimal getAmount();
    }
}
