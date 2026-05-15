package com.perroamor.inventory.sales.infrastructure.persistence;

import com.perroamor.inventory.sales.domain.SaleFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SaleSpecifications {

    private SaleSpecifications() {
    }

    public static Specification<SaleJpaEntity> withFilter(SaleFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.eventId() != null) {
                predicates.add(cb.equal(root.get("event").get("id"), filter.eventId()));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), filter.to()));
            }
            if (filter.paymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }
            if (filter.isCancelled() != null) {
                predicates.add(cb.equal(root.get("isCancelled"), filter.isCancelled()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
