package com.perroamor.inventory.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<BrandJpaEntity, Long> {

    Optional<BrandJpaEntity> findByName(String name);

    List<BrandJpaEntity> findAllByIsActiveTrue();

    @Modifying
    @Query("UPDATE BrandJpaEntity b SET b.isActive = false WHERE b.id = :id")
    int softDelete(@Param("id") Long id);
}
