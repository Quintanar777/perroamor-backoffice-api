package com.perroamor.inventory.sales.application;

import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.events.application.EventService;
import com.perroamor.inventory.events.domain.Event;
import com.perroamor.inventory.events.domain.EventStatus;
import com.perroamor.inventory.sales.domain.CreateSaleCommand;
import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleFilter;
import com.perroamor.inventory.sales.domain.SaleItem;
import com.perroamor.inventory.sales.domain.SaleRepository;
import com.perroamor.inventory.sales.domain.SaleStats;
import com.perroamor.inventory.shared.error.BusinessRuleException;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final EventService eventService;
    private final ProductService productService;
    private final ProductVariantService variantService;

    public SaleService(SaleRepository saleRepository,
                       EventService eventService,
                       ProductService productService,
                       ProductVariantService variantService) {
        this.saleRepository = saleRepository;
        this.eventService = eventService;
        this.productService = productService;
        this.variantService = variantService;
    }

    public Page<Sale> search(SaleFilter filter, PageRequest pageRequest) {
        return saleRepository.search(filter, pageRequest);
    }

    public Sale getById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Venta", id));
    }

    public SaleStats stats(Long eventId) {
        eventService.getById(eventId);
        return saleRepository.statsByEvent(eventId);
    }

    @Transactional
    public Sale createSale(CreateSaleCommand command) {
        validateCommand(command);

        Event event = eventService.getById(command.eventId());
        if (event.status(LocalDate.now()) != EventStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "El evento no está en curso. Solo se pueden registrar ventas durante un evento activo.");
        }
        if (!event.isActive()) {
            throw new BusinessRuleException("El evento está inactivo.");
        }

        // Bloqueo en orden estable para evitar deadlocks (productId asc, variantId asc).
        List<CreateSaleCommand.NewItem> sortedItems = command.items().stream()
                .sorted(Comparator
                        .comparing(CreateSaleCommand.NewItem::productId)
                        .thenComparing(i -> i.variantId() == null ? Long.MIN_VALUE : i.variantId()))
                .toList();

        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<SaleItem> snapshotItems = new ArrayList<>();

        for (CreateSaleCommand.NewItem item : sortedItems) {
            BigDecimal unitPrice;
            if (item.variantId() != null) {
                ProductVariant beforeDecrement = variantService.getById(item.variantId());
                if (!beforeDecrement.productId().equals(item.productId())) {
                    throw new ValidationException(
                            "La variante " + item.variantId() + " no pertenece al producto " + item.productId() + ".");
                }
                Product product = productService.getById(item.productId());
                unitPrice = product.price().add(
                        beforeDecrement.priceAdjustment() == null ? BigDecimal.ZERO : beforeDecrement.priceAdjustment());
                variantService.decrementStock(item.variantId(), item.quantity());
            } else {
                Product product = productService.getById(item.productId());
                if (product.hasVariants()) {
                    throw new ValidationException(
                            "El producto '" + product.name() + "' tiene variantes; debés especificar variantId.");
                }
                unitPrice = product.price();
                productService.decrementStock(item.productId(), item.quantity());
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
            itemsTotal = itemsTotal.add(lineTotal);

            snapshotItems.add(new SaleItem(
                    null,
                    null,
                    item.productId(),
                    item.variantId(),
                    item.quantity(),
                    unitPrice,
                    item.personalization(),
                    lineTotal));
        }

        BigDecimal discount = command.discountAmount() == null ? BigDecimal.ZERO : command.discountAmount();
        BigDecimal tax      = command.taxAmount()      == null ? BigDecimal.ZERO : command.taxAmount();
        BigDecimal total    = itemsTotal.subtract(discount).add(tax);
        if (total.signum() < 0) {
            throw new BusinessRuleException("El total de la venta no puede ser negativo.");
        }

        Sale toSave = new Sale(
                null,
                command.eventId(),
                command.soldByUserId(),
                null,
                LocalDateTime.now(),
                command.paymentMethod(),
                command.customerName(),
                command.customerPhone(),
                command.customerEmail(),
                command.notes(),
                discount,
                tax,
                total,
                command.isPaid(),
                false,
                null,
                null,
                snapshotItems);

        return saleRepository.save(toSave);
    }

    @Transactional
    public Sale cancelSale(Long id) {
        Sale existing = getById(id);
        if (existing.isCancelled()) {
            return existing;
        }
        for (SaleItem item : existing.items()) {
            if (item.variantId() != null) {
                variantService.incrementStock(item.variantId(), item.quantity());
            } else {
                productService.incrementStock(item.productId(), item.quantity());
            }
        }
        return saleRepository.markCancelled(id);
    }

    private void validateCommand(CreateSaleCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new ValidationException("La venta debe tener al menos un ítem.");
        }
        if (command.paymentMethod() == null) {
            throw new ValidationException("El método de pago es obligatorio.");
        }
        for (CreateSaleCommand.NewItem item : command.items()) {
            if (item.productId() == null) {
                throw new ValidationException("Todos los ítems deben referenciar un productId.");
            }
            if (item.quantity() <= 0) {
                throw new ValidationException("La cantidad de cada ítem debe ser mayor a cero.");
            }
        }
        if (command.discountAmount() != null && command.discountAmount().signum() < 0) {
            throw new ValidationException("El descuento no puede ser negativo.");
        }
        if (command.taxAmount() != null && command.taxAmount().signum() < 0) {
            throw new ValidationException("Los impuestos no pueden ser negativos.");
        }
    }
}
