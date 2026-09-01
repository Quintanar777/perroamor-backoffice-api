package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscountJpaRepository extends
        JpaRepository<DiscountJpaEntity, Long>,
        JpaSpecificationExecutor<DiscountJpaEntity> {

    List<DiscountJpaEntity> findByIsActiveTrue();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiscountJpaEntity d SET d.isActive = false WHERE d.id = :id")
    int softDelete(@Param("id") Long id);
}
