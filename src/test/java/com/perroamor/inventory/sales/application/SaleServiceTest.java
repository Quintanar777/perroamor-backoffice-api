package com.perroamor.inventory.sales.application;

import com.perroamor.inventory.catalog.application.BrandService;
import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
import com.perroamor.inventory.catalog.combos.domain.ComboRepository;
import com.perroamor.inventory.catalog.discounts.application.DiscountService;
import com.perroamor.inventory.catalog.discounts.domain.CreateDiscountCommand;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
import com.perroamor.inventory.catalog.discounts.domain.DiscountRepository;
import com.perroamor.inventory.catalog.discounts.domain.SlotType;
import com.perroamor.inventory.catalog.domain.Brand;
import com.perroamor.inventory.catalog.domain.BrandRepository;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductFilter;
import com.perroamor.inventory.catalog.domain.ProductRepository;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.catalog.domain.ProductVariantRepository;
import com.perroamor.inventory.events.application.EventService;
import com.perroamor.inventory.events.domain.Event;
import com.perroamor.inventory.events.domain.EventFilter;
import com.perroamor.inventory.events.domain.EventRepository;
import com.perroamor.inventory.sales.domain.CreateSaleCommand;
import com.perroamor.inventory.sales.domain.PaymentMethod;
import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleFilter;
import com.perroamor.inventory.sales.domain.SaleItem;
import com.perroamor.inventory.sales.domain.SaleRepository;
import com.perroamor.inventory.sales.domain.SaleStats;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SaleService repricing tests: line split (k discounted / n-k full),
 * unitPriceOverride precedence, sale-level discountAmount stacking,
 * comboId/variantId rejection, wholesale skip. Plain JUnit 5, real
 * application-layer services wired with hand-written stub repository ports
 * (no Spring context, no Mockito).
 */
class SaleServiceTest {

    private static final Long PRODUCT_A = 1L;
    private static final Long PRODUCT_B = 2L;
    private static final Long EVENT_ID = 100L;
    private static final Long USER_ID = 200L;

    private StubProductRepository productRepository;
    private StubSaleRepository saleRepository;
    private StubDiscountRepository discountRepository;
    private SaleService saleService;

    @BeforeEach
    void setUp() {
        productRepository = new StubProductRepository();
        productRepository.put(product(PRODUCT_A, "Product A", new BigDecimal("100.00"), 10));
        productRepository.put(product(PRODUCT_B, "Product B", new BigDecimal("50.00"), 10));

        StubBrandRepository brandRepository = new StubBrandRepository();
        BrandService brandService = new BrandService(brandRepository);
        ProductService productService = new ProductService(productRepository, brandService);

        StubProductVariantRepository variantRepository = new StubProductVariantRepository();
        ProductVariantService variantService = new ProductVariantService(variantRepository, productService);

        StubComboRepository comboRepository = new StubComboRepository();
        ComboService comboService = new ComboService(comboRepository, brandService, productService, variantService);

        StubEventRepository eventRepository = new StubEventRepository();
        eventRepository.put(activeInProgressEvent(EVENT_ID));
        EventService eventService = new EventService(eventRepository);

        discountRepository = new StubDiscountRepository();
        DiscountService discountService = new DiscountService(discountRepository);

        saleRepository = new StubSaleRepository();

        saleService = new SaleService(saleRepository, eventService, productService, variantService,
                comboService, discountService);
    }

    @Test
    void lineSplitAndSaleLevelDiscountStackOnTopOfAutoDiscountedPrice() {
        // Discount: FIXED slot on A (qty 1, price 80.00) + FIXED slot on B (qty 1, price 20.00).
        // Cart: 3 units of A, 1 unit of B. B's scarcity (only 1 available, slot needs 1) caps
        // applications at 1, so only 1 of A's 3 units gets discounted -> k=1, n=3 split.
        seedDiscount(10L, "Combo A+B", new BigDecimal("100.00"),
                fixedSlot(0, PRODUCT_A, 1, "80.00"),
                fixedSlot(1, PRODUCT_B, 1, "20.00"));

        CreateSaleCommand command = new CreateSaleCommand(
                EVENT_ID, USER_ID, PaymentMethod.CASH, null, null, null, null,
                new BigDecimal("50.00"), BigDecimal.ZERO, true, false,
                List.of(
                        new CreateSaleCommand.NewItem(PRODUCT_A, null, null, 3, null, null),
                        new CreateSaleCommand.NewItem(PRODUCT_B, null, null, 1, null, null)));

        Sale sale = saleService.createSale(command);

        List<SaleItem> productAItems = sale.items().stream().filter(i -> PRODUCT_A.equals(i.productId())).toList();
        assertThat(productAItems).hasSize(2);

        SaleItem discountedA = productAItems.stream().filter(i -> i.discountId() != null).findFirst().orElseThrow();
        assertThat(discountedA.quantity()).isEqualTo(1);
        assertThat(discountedA.unitPrice()).isEqualByComparingTo("80.00");
        assertThat(discountedA.discountId()).isEqualTo(10L);
        assertThat(discountedA.discountName()).isEqualTo("Combo A+B");

        SaleItem fullPriceA = productAItems.stream().filter(i -> i.discountId() == null).findFirst().orElseThrow();
        assertThat(fullPriceA.quantity()).isEqualTo(2);
        assertThat(fullPriceA.unitPrice()).isEqualByComparingTo("100.00");

        List<SaleItem> productBItems = sale.items().stream().filter(i -> PRODUCT_B.equals(i.productId())).toList();
        assertThat(productBItems).hasSize(1);
        assertThat(productBItems.get(0).quantity()).isEqualTo(1);
        assertThat(productBItems.get(0).unitPrice()).isEqualByComparingTo("20.00");
        assertThat(productBItems.get(0).discountId()).isEqualTo(10L);

        // itemsTotal = (1*80 + 2*100) + (1*20) = 280 + 20 = 300.00
        // sale-level discountAmount (50.00) still subtracts on top of the already-discounted total.
        assertThat(sale.totalAmount()).isEqualByComparingTo("250.00");

        // Stock decremented by total command quantity regardless of discount split.
        assertThat(productRepository.findById(PRODUCT_A).orElseThrow().stock()).isEqualTo(7);
        assertThat(productRepository.findById(PRODUCT_B).orElseThrow().stock()).isEqualTo(9);
    }

    @Test
    void manualOverrideReplacesDiscountedPriceButKeepsDiscountIdForTraceability() {
        seedDiscount(20L, "Single A", new BigDecimal("80.00"),
                fixedSlot(0, PRODUCT_A, 1, "80.00"));

        CreateSaleCommand command = new CreateSaleCommand(
                EVENT_ID, USER_ID, PaymentMethod.CASH, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, true, false,
                List.of(new CreateSaleCommand.NewItem(PRODUCT_A, null, null, 1, new BigDecimal("999.00"), null)));

        Sale sale = saleService.createSale(command);

        assertThat(sale.items()).hasSize(1);
        SaleItem item = sale.items().get(0);
        assertThat(item.unitPrice()).isEqualByComparingTo("999.00");
        assertThat(item.discountId()).isEqualTo(20L);
        assertThat(item.discountName()).isEqualTo("Single A");
    }

    @Test
    void wholesaleSaleSkipsDiscountDetectionEntirely() {
        seedDiscount(30L, "Single A", new BigDecimal("80.00"),
                fixedSlot(0, PRODUCT_A, 1, "80.00"));

        CreateSaleCommand command = new CreateSaleCommand(
                EVENT_ID, USER_ID, PaymentMethod.CASH, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, true, true,
                List.of(new CreateSaleCommand.NewItem(PRODUCT_A, null, null, 1, null, null)));

        Sale sale = saleService.createSale(command);

        assertThat(sale.items()).hasSize(1);
        SaleItem item = sale.items().get(0);
        assertThat(item.unitPrice()).isEqualByComparingTo("100.00");
        assertThat(item.discountId()).isNull();
    }

    @Test
    void saleWithComboIdIsRejected() {
        CreateSaleCommand command = new CreateSaleCommand(
                EVENT_ID, USER_ID, PaymentMethod.CASH, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, true, false,
                List.of(new CreateSaleCommand.NewItem(null, null, 999L, 1, null, null)));

        assertThatThrownBy(() -> saleService.createSale(command))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void saleWithVariantIdIsRejected() {
        CreateSaleCommand command = new CreateSaleCommand(
                EVENT_ID, USER_ID, PaymentMethod.CASH, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, true, false,
                List.of(new CreateSaleCommand.NewItem(PRODUCT_A, 999L, null, 1, null, null)));

        assertThatThrownBy(() -> saleService.createSale(command))
                .isInstanceOf(ValidationException.class);
    }

    private void seedDiscount(Long id, String name, BigDecimal totalPrice, DiscountSeedSlot... slots) {
        List<com.perroamor.inventory.catalog.discounts.domain.DiscountSlot> domainSlots = new ArrayList<>();
        for (DiscountSeedSlot s : slots) {
            domainSlots.add(new com.perroamor.inventory.catalog.discounts.domain.DiscountSlot(
                    null, s.position, SlotType.FIXED, s.quantity,
                    List.of(new com.perroamor.inventory.catalog.discounts.domain.DiscountSlotOption(
                            null, s.productId, null, s.price))));
        }
        discountRepository.seed(new Discount(id, name, null, totalPrice, true, null, domainSlots));
    }

    private static DiscountSeedSlot fixedSlot(int position, Long productId, int quantity, String price) {
        return new DiscountSeedSlot(position, productId, quantity, new BigDecimal(price));
    }

    private record DiscountSeedSlot(int position, Long productId, int quantity, BigDecimal price) {
    }

    private static Product product(Long id, String name, BigDecimal price, int stock) {
        return new Product(id, name, null, 1L, "Perro Amor", null, "Accesorios", price, price, stock,
                null, false, false, true, null, null);
    }

    private static Event activeInProgressEvent(Long id) {
        return new Event(id, "Evento", "Loc", null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                true, null);
    }

    // ---- Minimal stub repository ports (unused methods throw UnsupportedOperationException) ----

    private static final class StubProductRepository implements ProductRepository {
        private final Map<Long, Product> products = new HashMap<>();

        void put(Product p) { products.put(p.id(), p); }

        @Override public Page<Product> search(ProductFilter filter, PageRequest pageRequest) { throw new UnsupportedOperationException(); }
        @Override public Optional<Product> findById(Long id) { return Optional.ofNullable(products.get(id)); }
        @Override public Optional<Product> findByCode(String code) { throw new UnsupportedOperationException(); }
        @Override public List<Product> findAllWithoutCode() { throw new UnsupportedOperationException(); }
        @Override public boolean existsByCode(String code) { throw new UnsupportedOperationException(); }
        @Override public boolean existsByCodeAndIdNot(String code, Long id) { throw new UnsupportedOperationException(); }
        @Override public Product save(Product product) { throw new UnsupportedOperationException(); }
        @Override public Product update(Product product) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
        @Override public Product adjustStock(Long id, int delta) { throw new UnsupportedOperationException(); }

        @Override
        public Product decrementStock(Long id, int quantity) {
            Product updated = products.get(id).withStock(products.get(id).stock() - quantity);
            products.put(id, updated);
            return updated;
        }

        @Override
        public Product incrementStock(Long id, int quantity) {
            Product updated = products.get(id).withStock(products.get(id).stock() + quantity);
            products.put(id, updated);
            return updated;
        }
    }

    private static final class StubBrandRepository implements BrandRepository {
        @Override public List<Brand> findAll(boolean includeInactive) { throw new UnsupportedOperationException(); }
        @Override public Optional<Brand> findById(Long id) { throw new UnsupportedOperationException(); }
        @Override public Optional<Brand> findByName(String name) { throw new UnsupportedOperationException(); }
        @Override public Brand save(Brand brand) { throw new UnsupportedOperationException(); }
        @Override public Brand update(Brand brand) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubProductVariantRepository implements ProductVariantRepository {
        @Override public List<ProductVariant> findByProductId(Long productId, boolean includeInactive) { throw new UnsupportedOperationException(); }
        @Override public Optional<ProductVariant> findById(Long id) { throw new UnsupportedOperationException(); }
        @Override public Optional<ProductVariant> findBySku(String sku) { throw new UnsupportedOperationException(); }
        @Override public boolean existsBySku(String sku) { throw new UnsupportedOperationException(); }
        @Override public boolean existsBySkuAndIdNot(String sku, Long id) { throw new UnsupportedOperationException(); }
        @Override public ProductVariant save(ProductVariant variant) { throw new UnsupportedOperationException(); }
        @Override public ProductVariant update(ProductVariant variant) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
        @Override public ProductVariant decrementStock(Long id, int quantity) { throw new UnsupportedOperationException(); }
        @Override public ProductVariant incrementStock(Long id, int quantity) { throw new UnsupportedOperationException(); }
    }

    private static final class StubComboRepository implements ComboRepository {
        @Override public Page<Combo> search(ComboFilter filter, PageRequest pageRequest) { throw new UnsupportedOperationException(); }
        @Override public Optional<Combo> findById(Long id) { throw new UnsupportedOperationException(); }
        @Override public Combo save(Combo combo) { throw new UnsupportedOperationException(); }
        @Override public Combo replace(Combo combo) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubEventRepository implements EventRepository {
        private final Map<Long, Event> events = new HashMap<>();

        void put(Event e) { events.put(e.id(), e); }

        @Override public Page<Event> search(EventFilter filter, LocalDate today, PageRequest pageRequest) { throw new UnsupportedOperationException(); }
        @Override public Optional<Event> findById(Long id) { return Optional.ofNullable(events.get(id)); }
        @Override public Optional<Event> findCurrent(LocalDate today) { throw new UnsupportedOperationException(); }
        @Override public Event save(Event event) { throw new UnsupportedOperationException(); }
        @Override public Event update(Event event) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubDiscountRepository implements DiscountRepository {
        private final List<Discount> discounts = new ArrayList<>();

        void seed(Discount discount) { discounts.add(discount); }

        @Override public Page<Discount> search(DiscountFilter filter, PageRequest pageRequest) { throw new UnsupportedOperationException(); }
        @Override public Optional<Discount> findById(Long id) { throw new UnsupportedOperationException(); }
        @Override public List<Discount> findActive() { return discounts.stream().filter(Discount::isActive).toList(); }
        @Override public Discount save(Discount discount) { throw new UnsupportedOperationException(); }
        @Override public Discount replace(Discount discount) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubSaleRepository implements SaleRepository {
        private long nextId = 1;

        @Override public Page<Sale> search(SaleFilter filter, PageRequest pageRequest) { throw new UnsupportedOperationException(); }
        @Override public Optional<Sale> findById(Long id) { throw new UnsupportedOperationException(); }

        @Override
        public Sale save(Sale sale) {
            return new Sale(nextId++, sale.eventId(), sale.soldByUserId(), "tester", sale.saleDate(),
                    sale.paymentMethod(), sale.customerName(), sale.customerPhone(), sale.customerEmail(),
                    sale.notes(), sale.discountAmount(), sale.taxAmount(), sale.totalAmount(), sale.isPaid(),
                    sale.isWholesale(), sale.isCancelled(), sale.cancelledAt(), sale.createdAt(), sale.items());
        }

        @Override public Sale markCancelled(Long id) { throw new UnsupportedOperationException(); }
        @Override public SaleStats stats(SaleFilter filter) { throw new UnsupportedOperationException(); }
    }
}
