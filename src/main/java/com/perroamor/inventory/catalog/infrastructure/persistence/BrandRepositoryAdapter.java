package com.perroamor.inventory.catalog.infrastructure.persistence;

import com.perroamor.inventory.catalog.domain.Brand;
import com.perroamor.inventory.catalog.domain.BrandRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BrandRepositoryAdapter implements BrandRepository {

    private final BrandJpaRepository jpa;
    private final BrandMapper mapper;

    public BrandRepositoryAdapter(BrandJpaRepository jpa, BrandMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findAll(boolean includeInactive) {
        List<BrandJpaEntity> entities = includeInactive ? jpa.findAll() : jpa.findAllByIsActiveTrue();
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findByName(String name) {
        return jpa.findByName(name).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Brand save(Brand brand) {
        BrandJpaEntity entity = mapper.toEntity(brand);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public Brand update(Brand brand) {
        BrandJpaEntity existing = jpa.findById(brand.id())
                .orElseThrow(() -> new IllegalStateException("Brand " + brand.id() + " no encontrado"));
        existing.setName(brand.name());
        existing.setDescription(brand.description());
        existing.setActive(brand.isActive());
        return mapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }
}
