package com.perroamor.inventory.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository {

    List<ProductVariant> findByProductId(Long productId, boolean includeInactive);

    Optional<ProductVariant> findById(Long id);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    ProductVariant save(ProductVariant variant);

    ProductVariant update(ProductVariant variant);

    void softDelete(Long id);

    ProductVariant decrementStock(Long id, int quantity);

    ProductVariant incrementStock(Long id, int quantity);
}
