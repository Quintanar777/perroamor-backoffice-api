package com.perroamor.inventory.catalog.combos.infrastructure.web;

import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import org.springframework.stereotype.Component;

/**
 * inventory-2-0: combos are read-only. {@code toCreateCommand}/{@code toUpdateCommand}
 * were removed along with {@code ComboRequest}, {@code CreateComboCommand}, and
 * {@code UpdateComboCommand} (combo-retirement — no write endpoints remain).
 */
@Component
public class ComboWebMapper {

    private final ComboService comboService;

    public ComboWebMapper(ComboService comboService) {
        this.comboService = comboService;
    }

    public ComboResponse toResponse(Combo combo) {
        return new ComboResponse(
                combo.id(),
                combo.name(),
                combo.description(),
                combo.brandId(),
                combo.brandName(),
                combo.brandColor(),
                combo.price(),
                combo.wholesalePrice(),
                combo.isActive(),
                comboService.availableStock(combo),
                combo.createdAt(),
                combo.items().stream().map(this::toItemResponse).toList());
    }

    private ComboItemResponse toItemResponse(ComboItem item) {
        return new ComboItemResponse(
                item.id(),
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.quantity());
    }
}
