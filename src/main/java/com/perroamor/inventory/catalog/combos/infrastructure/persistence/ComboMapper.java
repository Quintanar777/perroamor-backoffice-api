package com.perroamor.inventory.catalog.combos.infrastructure.persistence;

import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;

import java.util.List;

public final class ComboMapper {

    private ComboMapper() {
    }

    public static ComboItem toDomain(ComboItemJpaEntity entity) {
        return new ComboItem(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getVariant() != null ? entity.getVariant().getId() : null,
                entity.getVariant() != null ? entity.getVariant().getVariantName() : null,
                entity.getQuantity());
    }

    public static Combo toDomain(ComboJpaEntity entity) {
        List<ComboItem> items = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream().map(ComboMapper::toDomain).toList();

        return new Combo(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBrand().getId(),
                entity.getBrand().getName(),
                entity.getBrand().getBaseColor(),
                entity.getPrice(),
                entity.getWholesalePrice(),
                entity.isActive(),
                entity.getCreatedAt(),
                items);
    }
}
