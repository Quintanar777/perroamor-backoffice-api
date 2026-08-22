package com.perroamor.inventory.catalog.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, Long> {

    List<ProductVariantJpaEntity> findAllByProductIdOrderByIdAsc(Long productId);

    List<ProductVariantJpaEntity> findAllByProductIdAndIsActiveTrueOrderByIdAsc(Long productId);

    Optional<ProductVariantJpaEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Modifying
    @Query("UPDATE ProductVariantJpaEntity v SET v.isActive = false WHERE v.id = :id")
    int softDelete(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ProductVariantJpaEntity v WHERE v.product.id = :productId AND v.isActive = true")
    int sumActiveStockByProductId(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariantJpaEntity v WHERE v.id = :id")
    Optional<ProductVariantJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
