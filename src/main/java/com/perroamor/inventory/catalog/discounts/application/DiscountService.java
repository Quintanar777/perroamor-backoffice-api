package com.perroamor.inventory.catalog.discounts.application;

import com.perroamor.inventory.catalog.discounts.domain.CreateDiscountCommand;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
import com.perroamor.inventory.catalog.discounts.domain.DiscountRepository;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlot;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlotOption;
import com.perroamor.inventory.catalog.discounts.domain.SlotType;
import com.perroamor.inventory.catalog.discounts.domain.UpdateDiscountCommand;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public Page<Discount> search(DiscountFilter filter, PageRequest pageRequest) {
        return discountRepository.search(filter, pageRequest);
    }

    public Discount getById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Descuento", id));
    }

    public List<Discount> findActive() {
        return discountRepository.findActive();
    }

    public Discount create(CreateDiscountCommand command) {
        validateSlots(command.slots());
        validateTotal(command.totalPrice(), command.slots());

        Discount toSave = new Discount(
                null,
                command.name(),
                command.description(),
                command.totalPrice(),
                true,
                null,
                toSlots(command.slots()));

        return discountRepository.save(toSave);
    }

    public Discount update(Long id, UpdateDiscountCommand command) {
        Discount existing = getById(id);
        validateSlots(command.slots());
        validateTotal(command.totalPrice(), command.slots());

        Discount updated = new Discount(
                existing.id(),
                command.name(),
                command.description(),
                command.totalPrice(),
                command.isActive(),
                existing.createdAt(),
                toSlots(command.slots()));

        return discountRepository.replace(updated);
    }

    public void delete(Long id) {
        getById(id);
        discountRepository.softDelete(id);
    }

    private void validateSlots(List<CreateDiscountCommand.NewSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new ValidationException("El descuento debe tener al menos un slot.");
        }
        for (CreateDiscountCommand.NewSlot slot : slots) {
            if (slot.options() == null || slot.options().isEmpty()) {
                throw new ValidationException(
                        "El slot en posición " + slot.position() + " debe tener al menos una opción.");
            }
            if (slot.quantity() <= 0) {
                throw new ValidationException(
                        "La cantidad del slot en posición " + slot.position() + " debe ser mayor a cero.");
            }
            if (slot.slotType() == SlotType.GROUP && slot.quantity() != 1) {
                throw new ValidationException(
                        "El slot en posición " + slot.position() + " es GROUP y debe tener cantidad igual a 1.");
            }
            if (slot.slotType() == SlotType.FIXED && slot.options().size() != 1) {
                throw new ValidationException(
                        "El slot en posición " + slot.position() + " es FIXED y debe tener exactamente una opción.");
            }
            for (CreateDiscountCommand.NewOption option : slot.options()) {
                if (option.productId() == null) {
                    throw new ValidationException(
                            "Cada opción del slot en posición " + slot.position() + " debe tener productId.");
                }
            }
        }
    }

    /**
     * Decisión de diseño (design.md "Total validation"): exigir que todas las
     * opciones DENTRO de un mismo slot compartan un único final_unit_price y
     * luego validar que Σ(slot.price × slot.quantity) == total_price es
     * algebraicamente equivalente a validar cada combinación explícita
     * (fixed + una opción por cada group), pero en O(n) en vez de combinatoria.
     */
    private void validateTotal(BigDecimal totalPrice, List<CreateDiscountCommand.NewSlot> slots) {
        if (totalPrice == null || totalPrice.signum() < 0) {
            throw new ValidationException("El precio total del descuento no puede ser negativo.");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (CreateDiscountCommand.NewSlot slot : slots) {
            BigDecimal slotPrice = null;
            for (CreateDiscountCommand.NewOption option : slot.options()) {
                if (option.finalUnitPrice() == null || option.finalUnitPrice().signum() < 0) {
                    throw new ValidationException(
                            "El precio final de cada opción del slot en posición " + slot.position() +
                            " no puede ser negativo.");
                }
                if (slotPrice == null) {
                    slotPrice = option.finalUnitPrice();
                } else if (slotPrice.compareTo(option.finalUnitPrice()) != 0) {
                    throw new ValidationException(
                            "Las opciones del slot en posición " + slot.position() +
                            " deben compartir el mismo precio final.");
                }
            }
            sum = sum.add(slotPrice.multiply(BigDecimal.valueOf(slot.quantity())));
        }
        if (sum.compareTo(totalPrice) != 0) {
            throw new ValidationException(
                    "La suma de los slots (" + sum + ") no coincide con el total declarado (" + totalPrice + ").");
        }
    }

    private List<DiscountSlot> toSlots(List<CreateDiscountCommand.NewSlot> newSlots) {
        List<DiscountSlot> slots = new ArrayList<>(newSlots.size());
        for (CreateDiscountCommand.NewSlot newSlot : newSlots) {
            List<DiscountSlotOption> options = new ArrayList<>(newSlot.options().size());
            for (CreateDiscountCommand.NewOption newOption : newSlot.options()) {
                options.add(new DiscountSlotOption(null, newOption.productId(), null, newOption.finalUnitPrice()));
            }
            slots.add(new DiscountSlot(null, newSlot.position(), newSlot.slotType(), newSlot.quantity(), options));
        }
        return slots;
    }
}
