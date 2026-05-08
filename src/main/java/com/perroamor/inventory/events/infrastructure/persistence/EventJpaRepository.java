package com.perroamor.inventory.events.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface EventJpaRepository extends
        JpaRepository<EventJpaEntity, Long>,
        JpaSpecificationExecutor<EventJpaEntity> {

    @Query("""
            SELECT e FROM EventJpaEntity e
            WHERE e.isActive = true
              AND e.startDate <= :today
              AND e.endDate   >= :today
            ORDER BY e.startDate ASC
            """)
    Optional<EventJpaEntity> findCurrent(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE EventJpaEntity e SET e.isActive = false WHERE e.id = :id")
    int softDelete(@Param("id") Long id);
}
