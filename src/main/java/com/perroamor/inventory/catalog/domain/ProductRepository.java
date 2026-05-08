package com.perroamor.inventory.catalog.domain;

import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;

import java.util.Optional;

public interface ProductRepository {

    Page<Product> search(ProductFilter filter, PageRequest pageRequest);

    Optional<Product> findById(Long id);

    Product save(Product product);

    Product update(Product product);

    void softDelete(Long id);

    Product adjustStock(Long id, int delta);
}
