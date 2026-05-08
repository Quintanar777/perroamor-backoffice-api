package com.perroamor.inventory.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {

    List<Brand> findAll(boolean includeInactive);

    Optional<Brand> findById(Long id);

    Optional<Brand> findByName(String name);

    Brand save(Brand brand);

    Brand update(Brand brand);

    void softDelete(Long id);
}
