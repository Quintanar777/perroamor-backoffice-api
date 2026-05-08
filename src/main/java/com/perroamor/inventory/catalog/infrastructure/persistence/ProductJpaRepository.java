package com.perroamor.inventory.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends
        JpaRepository<ProductJpaEntity, Long>,
        JpaSpecificationExecutor<ProductJpaEntity> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductJpaEntity p SET p.isActive = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int softDelete(@Param("id") Long id);
}
