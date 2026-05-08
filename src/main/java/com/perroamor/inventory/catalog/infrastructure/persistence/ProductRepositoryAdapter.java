package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductFilter;
import com.perroamor.inventory.catalog.domain.ProductRepository;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;
    private final BrandJpaRepository brandJpa;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository jpa,
                                    BrandJpaRepository brandJpa,
                                    ProductMapper mapper) {
        this.jpa = jpa;
        this.brandJpa = brandJpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(ProductFilter filter, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by("id").ascending());

        var jpaPage = jpa.findAll(ProductSpecifications.withFilter(filter), pageable);

        var content = jpaPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.page(), pageRequest.size(), jpaPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        BrandJpaEntity brandRef = brandJpa.getReferenceById(product.brandId());
        ProductJpaEntity entity = mapper.toEntity(product);
        entity.setBrand(brandRef);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public Product update(Product product) {
        ProductJpaEntity existing = jpa.findById(product.id())
                .orElseThrow(() -> new IllegalStateException("Product " + product.id() + " no encontrado"));

        if (existing.getBrand() == null || !existing.getBrand().getId().equals(product.brandId())) {
            existing.setBrand(brandJpa.getReferenceById(product.brandId()));
        }
        existing.setName(product.name());
        existing.setCategory(product.category());
        existing.setPrice(product.price());
        existing.setWholesalePrice(product.wholesalePrice());
        existing.setStock(product.stock());
        existing.setDescription(product.description());
        existing.setCanBePersonalized(product.canBePersonalized());
        existing.setHasVariants(product.hasVariants());
        existing.setActive(product.isActive());
        return mapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }

    @Override
    @Transactional
    public Product adjustStock(Long id, int delta) {
        ProductJpaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product " + id + " no encontrado"));
        int next = entity.getStock() + delta;
        if (next < 0) {
            throw new IllegalStateException("Stock no puede quedar negativo (stock actual " + entity.getStock() + ", delta " + delta + ").");
        }
        entity.setStock(next);
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }
}
