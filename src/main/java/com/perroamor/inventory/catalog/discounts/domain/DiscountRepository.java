package com.perroamor.inventory.catalog.discounts.domain;

import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository {

    Page<Discount> search(DiscountFilter filter, PageRequest pageRequest);

    Optional<Discount> findById(Long id);

    List<Discount> findActive();

    Discount save(Discount discount);

    Discount replace(Discount discount);

    void softDelete(Long id);
}
