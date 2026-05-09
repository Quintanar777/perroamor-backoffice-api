package com.perroamor.inventory.sales.infrastructure.persistence;

import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleItem;

import java.util.List;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static SaleItem toDomain(SaleItemJpaEntity entity) {
        return new SaleItem(
                entity.getId(),
                entity.getSale() != null ? entity.getSale().getId() : null,
                entity.getProduct() != null ? entity.getProduct().getId() : null,
                entity.getProduct() != null ? entity.getProduct().getName() : null,
                entity.getVariant() != null ? entity.getVariant().getId() : null,
                entity.getVariant() != null ? entity.getVariant().getVariantName() : null,
                entity.getCombo() != null ? entity.getCombo().getId() : null,
                entity.getCombo() != null ? entity.getCombo().getName() : null,
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getPersonalization(),
                entity.getLineTotal());
    }

    public static Sale toDomain(SaleJpaEntity entity) {
        List<SaleItem> items = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream().map(SaleMapper::toDomain).toList();

        return new Sale(
                entity.getId(),
                entity.getEvent().getId(),
                entity.getSoldBy().getId(),
                entity.getSoldBy().getUsername(),
                entity.getSaleDate(),
                entity.getPaymentMethod(),
                entity.getCustomerName(),
                entity.getCustomerPhone(),
                entity.getCustomerEmail(),
                entity.getNotes(),
                entity.getDiscountAmount(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.isPaid(),
                entity.isCancelled(),
                entity.getCancelledAt(),
                entity.getCreatedAt(),
                items);
    }
}
