package com.perroamor.inventory.catalog.combos.infrastructure.web;

import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import com.perroamor.inventory.catalog.combos.domain.CreateComboCommand;
import com.perroamor.inventory.catalog.combos.domain.UpdateComboCommand;
import org.springframework.stereotype.Component;

@Component
public class ComboWebMapper {

    private final ComboService comboService;

    public ComboWebMapper(ComboService comboService) {
        this.comboService = comboService;
    }

    public CreateComboCommand toCreateCommand(ComboRequest request) {
        return new CreateComboCommand(
                request.name(),
                request.description(),
                request.brandId(),
                request.price(),
                request.wholesalePrice(),
                request.items().stream()
                        .map(i -> new CreateComboCommand.NewItem(i.productId(), i.variantId(), i.quantity()))
                        .toList());
    }

    public UpdateComboCommand toUpdateCommand(ComboRequest request) {
        return new UpdateComboCommand(
                request.name(),
                request.description(),
                request.brandId(),
                request.price(),
                request.wholesalePrice(),
                request.isActive() == null || request.isActive(),
                request.items().stream()
                        .map(i -> new CreateComboCommand.NewItem(i.productId(), i.variantId(), i.quantity()))
                        .toList());
    }

    public ComboResponse toResponse(Combo combo) {
        return new ComboResponse(
                combo.id(),
                combo.name(),
                combo.description(),
                combo.brandId(),
                combo.brandName(),
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
