package com.perroamor.inventory.sales.domain;

import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;

import java.util.Optional;

public interface SaleRepository {

    Page<Sale> search(SaleFilter filter, PageRequest pageRequest);

    Optional<Sale> findById(Long id);

    Sale save(Sale sale);

    Sale markCancelled(Long id);

    SaleStats stats(SaleFilter filter);
}
