package com.perroamor.inventory.sales.application;

import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import com.perroamor.inventory.catalog.discounts.application.DiscountService;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountMatcher;
import com.perroamor.inventory.catalog.domain.Product;
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

import io.vavr.control.Option;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final EventService eventService;
    private final ProductService productService;
    private final ProductVariantService variantService;
    private final ComboService comboService;
    private final DiscountService discountService;

    public SaleService(SaleRepository saleRepository,
                       EventService eventService,
                       ProductService productService,
                       ProductVariantService variantService,
                       ComboService comboService,
                       DiscountService discountService) {
        this.saleRepository = saleRepository;
        this.eventService = eventService;
        this.productService = productService;
        this.variantService = variantService;
        this.comboService = comboService;
        this.discountService = discountService;
    }

    public Page<Sale> search(SaleFilter filter, PageRequest pageRequest) {
        return saleRepository.search(filter, pageRequest);
    }

    public Sale getById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Venta", id));
    }

    public SaleStats stats(SaleFilter filter) {
        eventService.getById(filter.eventId());
        return saleRepository.stats(filter);
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

        // Resolver cada item: solo producto suelto. comboId/variantId ya fueron rechazados en
        // validateCommand -- inventory-2-0 retira los write-paths de combo/variante de venta.
        List<ResolvedItem> resolvedItems = new ArrayList<>();
        for (CreateSaleCommand.NewItem item : command.items()) {
            Product product = productService.getById(item.productId());
            if (!product.isActive()) {
                throw new BusinessRuleException("El producto '" + product.name() + "' está inactivo.");
            }
            resolvedItems.add(new ResolvedItem(item, product));
        }

        // Detección de descuento automático: opera sobre la disponibilidad total del carrito
        // (agrupada por producto), antes de decrementar stock. Se salta por completo en ventas
        // de mayoreo -- ninguna línea se inspecciona ni repricea.
        Map<Long, Integer> availableByProduct = new HashMap<>();
        for (ResolvedItem ri : resolvedItems) {
            availableByProduct.merge(ri.product().id(), ri.item().quantity(), Integer::sum);
        }

        Optional<DiscountMatcher.MatchResult> matchResult = Optional.empty();
        if (!command.isWholesale()) {
            List<Discount> activeDiscounts = discountService.findActive();
            matchResult = DiscountMatcher.match(availableByProduct, activeDiscounts);
        }

        // Acumular stock a descontar por producto -- un solo decrement por producto,
        // independiente del split de líneas por descuento.
        Map<Long, Integer> totalDecrements = new HashMap<>();
        for (ResolvedItem ri : resolvedItems) {
            totalDecrements.merge(ri.product().id(), ri.item().quantity(), Integer::sum);
        }
        totalDecrements.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> productService.decrementStock(e.getKey(), e.getValue()));

        // Construir sale_items snapshot. Cuando el descuento consume k de las n unidades de un
        // item, se emiten dos filas: k @ precio final (con discount_id, trazabilidad) y n-k @
        // precio original. unitPriceOverride, si viene, reemplaza el precio de AMBAS filas pero
        // NO borra el discount_id de la fila que sí fue repriceada -- ver spec "Manual Line Edit
        // Overrides Discount Price".
        Long discountId = matchResult.map(DiscountMatcher.MatchResult::discountId).orElse(null);
        String discountName = matchResult.map(DiscountMatcher.MatchResult::discountName).orElse(null);
        Map<Long, Integer> remainingConsumed = new HashMap<>();
        Map<Long, BigDecimal> discountPriceByProduct = new HashMap<>();
        matchResult.ifPresent(m -> m.consumedByProduct().forEach((productId, consumed) -> {
            remainingConsumed.put(productId, consumed.quantity());
            discountPriceByProduct.put(productId, consumed.finalUnitPrice());
        }));

        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<SaleItem> snapshotItems = new ArrayList<>();
        for (ResolvedItem ri : resolvedItems) {
            Option<BigDecimal> override = Option.of(ri.item().unitPriceOverride());
            int n = ri.item().quantity();
            int available = remainingConsumed.getOrDefault(ri.product().id(), 0);
            int k = Math.min(available, n);

            if (k > 0) {
                BigDecimal discountedPrice = override.getOrElse(discountPriceByProduct.get(ri.product().id()));
                BigDecimal lineTotal = discountedPrice.multiply(BigDecimal.valueOf(k));
                itemsTotal = itemsTotal.add(lineTotal);
                snapshotItems.add(new SaleItem(
                        null, null, ri.product().id(), ri.product().name(), null, null, null, null,
                        discountId, discountName, k, discountedPrice, ri.item().personalization(), lineTotal));
                remainingConsumed.put(ri.product().id(), available - k);
            }

            int remainder = n - k;
            if (remainder > 0) {
                BigDecimal unitPrice = override.getOrElse(ri.product().price());
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(remainder));
                itemsTotal = itemsTotal.add(lineTotal);
                snapshotItems.add(new SaleItem(
                        null, null, ri.product().id(), ri.product().name(), null, null, null, null,
                        null, null, remainder, unitPrice, ri.item().personalization(), lineTotal));
            }
        }

        BigDecimal discount = Option.of(command.discountAmount()).getOrElse(BigDecimal.ZERO);
        BigDecimal tax      = Option.of(command.taxAmount()).getOrElse(BigDecimal.ZERO);
        BigDecimal total    = itemsTotal.subtract(discount).add(tax);
        if (total.signum() < 0) {
            throw new BusinessRuleException("El total de la venta no puede ser negativo.");
        }

        Sale toSave = new Sale(
                null,
                command.eventId(),
                command.soldByUserId(),
                null,
                LocalDateTime.now(ZoneId.of("America/Mexico_City")),
                command.paymentMethod(),
                command.customerName(),
                command.customerPhone(),
                command.customerEmail(),
                command.notes(),
                discount,
                tax,
                total,
                command.isPaid(),
                command.isWholesale(),
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
            // inventory-2-0: combos y variantes ya no se venden directamente. Los combos
            // existentes quedan de solo lectura (historial); las variantes se reemplazan por
            // productos independientes uno-por-talla.
            if (item.comboId() != null) {
                throw new ValidationException(
                        "Los combos ya no se pueden vender directamente; son de solo lectura para historial.");
            }
            if (item.variantId() != null) {
                throw new ValidationException(
                        "Las variantes ya no se pueden vender directamente; usa el producto correspondiente.");
            }
            if (item.productId() == null) {
                throw new ValidationException("Cada ítem debe referenciar un productId.");
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

    private record ResolvedItem(CreateSaleCommand.NewItem item, Product product) {
    }
}
