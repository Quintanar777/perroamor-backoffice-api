package com.perroamor.inventory.sales.application;

import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final EventService eventService;
    private final ProductService productService;
    private final ProductVariantService variantService;
    private final ComboService comboService;

    public SaleService(SaleRepository saleRepository,
                       EventService eventService,
                       ProductService productService,
                       ProductVariantService variantService,
                       ComboService comboService) {
        this.saleRepository = saleRepository;
        this.eventService = eventService;
        this.productService = productService;
        this.variantService = variantService;
        this.comboService = comboService;
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

        // Resolver cada item: producto suelto o combo (con sus componentes).
        List<ResolvedItem> resolvedItems = new ArrayList<>();
        for (CreateSaleCommand.NewItem item : command.items()) {
            if (item.comboId() != null) {
                Combo combo = comboService.getById(item.comboId());
                if (!combo.isActive()) {
                    throw new BusinessRuleException("El combo '" + combo.name() + "' está inactivo.");
                }
                resolvedItems.add(new ResolvedItem.OfCombo(item, combo));
            } else {
                Product product = productService.getById(item.productId());
                if (item.variantId() != null) {
                    ProductVariant variant = variantService.getById(item.variantId());
                    if (!variant.productId().equals(item.productId())) {
                        throw new ValidationException(
                                "La variante " + item.variantId() +
                                " no pertenece al producto " + item.productId() + ".");
                    }
                    if (!variant.isActive()) {
                        throw new BusinessRuleException(
                                "La variante '" + variant.variantName() + "' está inactiva.");
                    }
                    resolvedItems.add(new ResolvedItem.OfVariant(item, product, variant));
                } else {
                    // Producto sin variantId. Aceptamos esto incluso si el producto tiene
                    // hasVariants=true: en la práctica los empleados no eligen variante
                    // durante un evento con mucha gente. Cuando se vende sin variant,
                    // el descuento de stock va al campo product.stock (no a variants).
                    // Trade-off: product.stock puede no coincidir exactamente con sum(variants.stock).
                    if (!product.isActive()) {
                        throw new BusinessRuleException(
                                "El producto '" + product.name() + "' está inactivo.");
                    }
                    resolvedItems.add(new ResolvedItem.OfProduct(item, product));
                }
            }
        }

        // Acumular stock a descontar en un solo lugar (key = productId|variantId, qty total).
        // Si un mismo producto/variante aparece en varios items o como componente de combos,
        // se acumula y se hace un solo decrement por key — evita locks redundantes.
        Map<StockKey, Integer> totalDecrements = new HashMap<>();
        for (ResolvedItem ri : resolvedItems) {
            switch (ri) {
                case ResolvedItem.OfProduct p ->
                        addDecrement(totalDecrements,
                                new StockKey(p.product.id(), null), p.item.quantity());
                case ResolvedItem.OfVariant v ->
                        addDecrement(totalDecrements,
                                new StockKey(v.variant.productId(), v.variant.id()), v.item.quantity());
                case ResolvedItem.OfCombo c -> {
                    for (ComboItem comp : c.combo.items()) {
                        addDecrement(totalDecrements,
                                new StockKey(comp.productId(), comp.variantId()),
                                comp.quantity() * c.item.quantity());
                    }
                }
            }
        }

        // Ordenar y aplicar decrements en orden estable (productId asc, variantId asc) — anti-deadlock.
        // product.stock es el contador maestro: SIEMPRE se descuenta (con o sin variant).
        // Si la venta especifica variant, además se descuenta variant.stock para mantener el breakdown.
        totalDecrements.entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<StockKey, Integer>, Long>comparing(e -> e.getKey().productId)
                        .thenComparing(e -> e.getKey().variantId == null ? Long.MIN_VALUE : e.getKey().variantId))
                .forEach(e -> {
                    StockKey key = e.getKey();
                    int qty = e.getValue();
                    if (key.variantId != null) {
                        variantService.decrementStock(key.variantId, qty);
                    }
                    productService.decrementStock(key.productId, qty);
                });

        // Construir sale_items snapshot — uno por item original del command (combos NO se expanden acá).
        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<SaleItem> snapshotItems = new ArrayList<>();
        for (ResolvedItem ri : resolvedItems) {
            BigDecimal unitPrice;
            Long productId, variantId, comboId;
            String comboName;

            switch (ri) {
                case ResolvedItem.OfProduct p -> {
                    unitPrice = p.product.price();
                    productId = p.product.id();
                    variantId = null;
                    comboId = null;
                    comboName = null;
                }
                case ResolvedItem.OfVariant v -> {
                    BigDecimal adj = v.variant.priceAdjustment() == null ? BigDecimal.ZERO : v.variant.priceAdjustment();
                    unitPrice = v.product.price().add(adj);
                    productId = v.product.id();
                    variantId = v.variant.id();
                    comboId = null;
                    comboName = null;
                }
                case ResolvedItem.OfCombo c -> {
                    unitPrice = c.combo.price();
                    productId = null;
                    variantId = null;
                    comboId = c.combo.id();
                    comboName = c.combo.name();
                }
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ri.item().quantity()));
            itemsTotal = itemsTotal.add(lineTotal);

            snapshotItems.add(new SaleItem(
                    null,
                    null,
                    productId,
                    variantId,
                    comboId,
                    comboName,
                    ri.item().quantity(),
                    unitPrice,
                    ri.item().personalization(),
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

    /**
     * Cancela una venta y restituye stock atómicamente.
     * Idempotente: si ya estaba cancelada, devuelve sin hacer nada.
     *
     * NOTA sobre combos: la restitución de stock de un combo se calcula contra la composición
     * ACTUAL del combo. Si entre la venta y la cancelación cambió la composición (se agregaron/
     * removieron componentes), el stock se restituye a la versión nueva. Para MVP es aceptable;
     * si el negocio lo requiere, sería necesario persistir snapshot de la composición al vender.
     */
    @Transactional
    public Sale cancelSale(Long id) {
        Sale existing = getById(id);
        if (existing.isCancelled()) {
            return existing;
        }
        for (SaleItem item : existing.items()) {
            if (item.comboId() != null) {
                Combo combo = comboService.getById(item.comboId());
                for (ComboItem comp : combo.items()) {
                    int totalQty = comp.quantity() * item.quantity();
                    if (comp.variantId() != null) {
                        variantService.incrementStock(comp.variantId(), totalQty);
                    }
                    productService.incrementStock(comp.productId(), totalQty);
                }
            } else if (item.productId() != null) {
                if (item.variantId() != null) {
                    variantService.incrementStock(item.variantId(), item.quantity());
                }
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
            boolean hasCombo = item.comboId() != null;
            boolean hasProduct = item.productId() != null;
            if (hasCombo == hasProduct) {
                throw new ValidationException(
                        "Cada ítem debe referenciar productId O comboId, nunca ambos ni ninguno.");
            }
            if (hasCombo && item.variantId() != null) {
                throw new ValidationException(
                        "Un ítem de combo no puede tener variantId.");
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

    private static void addDecrement(Map<StockKey, Integer> map, StockKey key, int qty) {
        map.merge(key, qty, Integer::sum);
    }

    private record StockKey(Long productId, Long variantId) {
    }

    private sealed interface ResolvedItem {
        CreateSaleCommand.NewItem item();

        record OfProduct(CreateSaleCommand.NewItem item, Product product) implements ResolvedItem {
        }

        record OfVariant(CreateSaleCommand.NewItem item, Product product, ProductVariant variant) implements ResolvedItem {
        }

        record OfCombo(CreateSaleCommand.NewItem item, Combo combo) implements ResolvedItem {
        }
    }
}
