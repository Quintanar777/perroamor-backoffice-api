package com.perroamor.inventory.catalog.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends
        JpaRepository<ProductJpaEntity, Long>,
        JpaSpecificationExecutor<ProductJpaEntity> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductJpaEntity p SET p.isActive = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int softDelete(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.id = :id")
    Optional<ProductJpaEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<ProductJpaEntity> findByCode(String code);

    List<ProductJpaEntity> findAllByCodeIsNull();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
