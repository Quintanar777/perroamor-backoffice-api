package com.perroamor.inventory.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, Long> {

    List<ProductVariantJpaEntity> findAllByProductIdOrderByIdAsc(Long productId);

    List<ProductVariantJpaEntity> findAllByProductIdAndIsActiveTrueOrderByIdAsc(Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Modifying
    @Query("UPDATE ProductVariantJpaEntity v SET v.isActive = false WHERE v.id = :id")
    int softDelete(@Param("id") Long id);
}
