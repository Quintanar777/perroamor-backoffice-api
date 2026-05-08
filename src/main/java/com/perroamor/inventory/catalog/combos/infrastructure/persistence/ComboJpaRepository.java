package com.perroamor.inventory.catalog.combos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComboJpaRepository extends
        JpaRepository<ComboJpaEntity, Long>,
        JpaSpecificationExecutor<ComboJpaEntity> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ComboJpaEntity c SET c.isActive = false WHERE c.id = :id")
    int softDelete(@Param("id") Long id);
}
