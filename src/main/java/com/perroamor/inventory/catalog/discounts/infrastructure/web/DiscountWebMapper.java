package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import com.perroamor.inventory.catalog.discounts.domain.CreateDiscountCommand;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlot;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlotOption;
import com.perroamor.inventory.catalog.discounts.domain.UpdateDiscountCommand;
import org.springframework.stereotype.Component;

@Component
public class DiscountWebMapper {

    public CreateDiscountCommand toCreateCommand(DiscountRequest request) {
        return new CreateDiscountCommand(
                request.name(),
                request.description(),
                request.totalPrice(),
                request.slots().stream().map(this::toNewSlot).toList());
    }

    public UpdateDiscountCommand toUpdateCommand(DiscountRequest request) {
        return new UpdateDiscountCommand(
                request.name(),
                request.description(),
                request.totalPrice(),
                request.isActive() == null || request.isActive(),
                request.slots().stream().map(this::toNewSlot).toList());
    }

    private CreateDiscountCommand.NewSlot toNewSlot(DiscountSlotRequest request) {
        return new CreateDiscountCommand.NewSlot(
                request.position(),
                request.slotType(),
                request.quantity(),
                request.options().stream()
                        .map(o -> new CreateDiscountCommand.NewOption(o.productId(), o.finalUnitPrice()))
                        .toList());
    }

    public DiscountResponse toResponse(Discount discount) {
        return new DiscountResponse(
                discount.id(),
                discount.name(),
                discount.description(),
                discount.totalPrice(),
                discount.isActive(),
                discount.createdAt(),
                discount.slots().stream().map(this::toSlotResponse).toList());
    }

    private DiscountSlotResponse toSlotResponse(DiscountSlot slot) {
        return new DiscountSlotResponse(
                slot.id(),
                slot.position(),
                slot.slotType(),
                slot.quantity(),
                slot.options().stream().map(this::toOptionResponse).toList());
    }

    private DiscountSlotOptionResponse toOptionResponse(DiscountSlotOption option) {
        return new DiscountSlotOptionResponse(
                option.id(),
                option.productId(),
                option.productName(),
                option.finalUnitPrice());
    }
}
