package com.perroamor.inventory.catalog.combos.infrastructure.persistence;

import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import com.perroamor.inventory.catalog.combos.domain.ComboRepository;
import com.perroamor.inventory.catalog.infrastructure.persistence.BrandJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.BrandJpaRepository;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaRepository;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductVariantJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductVariantJpaRepository;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ComboRepositoryAdapter implements ComboRepository {

    private final ComboJpaRepository jpa;
    private final BrandJpaRepository brandJpa;
    private final ProductJpaRepository productJpa;
    private final ProductVariantJpaRepository variantJpa;

    public ComboRepositoryAdapter(ComboJpaRepository jpa,
                                  BrandJpaRepository brandJpa,
                                  ProductJpaRepository productJpa,
                                  ProductVariantJpaRepository variantJpa) {
        this.jpa = jpa;
        this.brandJpa = brandJpa;
        this.productJpa = productJpa;
        this.variantJpa = variantJpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Combo> search(ComboFilter filter, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by("id").ascending());

        var jpaPage = jpa.findAll(ComboSpecifications.withFilter(filter), pageable);
        var content = jpaPage.getContent().stream().map(ComboMapper::toDomain).toList();
        return Page.of(content, pageRequest.page(), pageRequest.size(), jpaPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Combo> findById(Long id) {
        return jpa.findById(id).map(ComboMapper::toDomain);
    }

    @Override
    @Transactional
    public Combo save(Combo combo) {
        BrandJpaEntity brandRef = brandJpa.getReferenceById(combo.brandId());
        ComboJpaEntity entity = new ComboJpaEntity();
        entity.setName(combo.name());
        entity.setDescription(combo.description());
        entity.setBrand(brandRef);
        entity.setPrice(combo.price());
        entity.setWholesalePrice(combo.wholesalePrice());
        entity.setActive(combo.isActive());

        for (ComboItem item : combo.items()) {
            entity.addItem(buildItemEntity(item));
        }

        return ComboMapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public Combo replace(Combo combo) {
        ComboJpaEntity existing = jpa.findById(combo.id())
                .orElseThrow(() -> NotFoundException.of("Combo", combo.id()));

        if (existing.getBrand() == null || !existing.getBrand().getId().equals(combo.brandId())) {
            existing.setBrand(brandJpa.getReferenceById(combo.brandId()));
        }
        existing.setName(combo.name());
        existing.setDescription(combo.description());
        existing.setPrice(combo.price());
        existing.setWholesalePrice(combo.wholesalePrice());
        existing.setActive(combo.isActive());

        existing.clearItems();
        for (ComboItem item : combo.items()) {
            existing.addItem(buildItemEntity(item));
        }

        return ComboMapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }

    private ComboItemJpaEntity buildItemEntity(ComboItem item) {
        ComboItemJpaEntity entity = new ComboItemJpaEntity();
        ProductJpaEntity productRef = productJpa.getReferenceById(item.productId());
        entity.setProduct(productRef);
        if (item.variantId() != null) {
            ProductVariantJpaEntity variantRef = variantJpa.getReferenceById(item.variantId());
            entity.setVariant(variantRef);
        }
        entity.setQuantity(item.quantity());
        return entity;
    }
}
