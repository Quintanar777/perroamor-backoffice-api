package com.perroamor.inventory.catalog.combos.domain;

import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;

import java.util.Optional;

public interface ComboRepository {

    Page<Combo> search(ComboFilter filter, PageRequest pageRequest);

    Optional<Combo> findById(Long id);

    Combo save(Combo combo);

    Combo replace(Combo combo);

    void softDelete(Long id);
}
