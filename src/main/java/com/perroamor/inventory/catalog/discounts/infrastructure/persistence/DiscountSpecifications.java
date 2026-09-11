package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DiscountSpecifications {

    private DiscountSpecifications() {
    }

    public static Specification<DiscountJpaEntity> withFilter(DiscountFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
            }
            if (filter.query() != null && !filter.query().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.query().toLowerCase() + "%"));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
