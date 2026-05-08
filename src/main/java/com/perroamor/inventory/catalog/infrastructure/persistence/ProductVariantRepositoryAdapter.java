package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.catalog.domain.ProductVariantRepository;
import com.perroamor.inventory.shared.error.BusinessRuleException;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductVariantRepositoryAdapter implements ProductVariantRepository {

    private final ProductVariantJpaRepository jpa;
    private final ProductJpaRepository productJpa;
    private final ProductVariantMapper mapper;

    public ProductVariantRepositoryAdapter(ProductVariantJpaRepository jpa,
                                           ProductJpaRepository productJpa,
                                           ProductVariantMapper mapper) {
        this.jpa = jpa;
        this.productJpa = productJpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> findByProductId(Long productId, boolean includeInactive) {
        List<ProductVariantJpaEntity> entities = includeInactive
                ? jpa.findAllByProductIdOrderByIdAsc(productId)
                : jpa.findAllByProductIdAndIsActiveTrueOrderByIdAsc(productId);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductVariant> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return jpa.existsBySku(sku);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySkuAndIdNot(String sku, Long id) {
        return jpa.existsBySkuAndIdNot(sku, id);
    }

    @Override
    @Transactional
    public ProductVariant save(ProductVariant variant) {
        ProductJpaEntity productRef = productJpa.getReferenceById(variant.productId());
        ProductVariantJpaEntity entity = mapper.toEntity(variant);
        entity.setProduct(productRef);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public ProductVariant update(ProductVariant variant) {
        ProductVariantJpaEntity existing = jpa.findById(variant.id())
                .orElseThrow(() -> new IllegalStateException("Variant " + variant.id() + " no encontrada"));
        existing.setVariantName(variant.variantName());
        existing.setColor(variant.color());
        existing.setSize(variant.size());
        existing.setDesign(variant.design());
        existing.setMaterial(variant.material());
        existing.setSku(variant.sku());
        existing.setStock(variant.stock());
        existing.setPriceAdjustment(variant.priceAdjustment());
        existing.setActive(variant.isActive());
        return mapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }

    @Override
    @Transactional
    public ProductVariant decrementStock(Long id, int quantity) {
        ProductVariantJpaEntity entity = jpa.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.of("Variante", id));
        if (entity.getStock() < quantity) {
            throw new BusinessRuleException(
                    "Stock insuficiente para variante '" + entity.getVariantName() +
                    "' (SKU " + entity.getSku() + ", disponible: " + entity.getStock() +
                    ", solicitado: " + quantity + ").");
        }
        entity.setStock(entity.getStock() - quantity);
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public ProductVariant incrementStock(Long id, int quantity) {
        ProductVariantJpaEntity entity = jpa.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.of("Variante", id));
        entity.setStock(entity.getStock() + quantity);
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }
}
