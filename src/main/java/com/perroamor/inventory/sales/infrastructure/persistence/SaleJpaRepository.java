package com.perroamor.inventory.sales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SaleJpaRepository extends
        JpaRepository<SaleJpaEntity, Long>,
        JpaSpecificationExecutor<SaleJpaEntity> {
}
