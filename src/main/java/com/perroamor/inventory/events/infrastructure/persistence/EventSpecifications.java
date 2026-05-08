package com.perroamor.inventory.events.infrastructure.persistence;

import com.perroamor.inventory.events.domain.EventFilter;
import com.perroamor.inventory.events.domain.EventStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<EventJpaEntity> withFilter(EventFilter filter, LocalDate today) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
            }

            if (filter.status() != null) {
                EventStatus s = filter.status();
                if (s == EventStatus.UPCOMING) {
                    predicates.add(cb.greaterThan(root.get("startDate"), today));
                } else if (s == EventStatus.IN_PROGRESS) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), today));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), today));
                } else { // FINISHED
                    predicates.add(cb.lessThan(root.get("endDate"), today));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
