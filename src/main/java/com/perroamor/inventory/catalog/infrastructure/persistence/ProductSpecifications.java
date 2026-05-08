package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<ProductJpaEntity> withFilter(ProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.brandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.brandId()));
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.category().toLowerCase()));
            }
            if (filter.query() != null && !filter.query().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.query().toLowerCase() + "%"));
            }
            if (filter.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
