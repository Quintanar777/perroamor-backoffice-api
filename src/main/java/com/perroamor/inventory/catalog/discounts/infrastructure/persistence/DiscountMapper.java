package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlot;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlotOption;

import java.util.List;

public final class DiscountMapper {

    private DiscountMapper() {
    }

    public static DiscountSlotOption toDomain(DiscountSlotOptionJpaEntity entity) {
        return new DiscountSlotOption(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getFinalUnitPrice());
    }

    public static DiscountSlot toDomain(DiscountSlotJpaEntity entity) {
        List<DiscountSlotOption> options = entity.getOptions() == null
                ? List.of()
                : entity.getOptions().stream().map(DiscountMapper::toDomain).toList();

        return new DiscountSlot(
                entity.getId(),
                entity.getPosition(),
                entity.getSlotType(),
                entity.getQuantity(),
                options);
    }

    public static Discount toDomain(DiscountJpaEntity entity) {
        List<DiscountSlot> slots = entity.getSlots() == null
                ? List.of()
                : entity.getSlots().stream().map(DiscountMapper::toDomain).toList();

        return new Discount(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTotalPrice(),
                entity.isActive(),
                entity.getCreatedAt(),
                slots);
    }
}
