package com.perroamor.inventory.catalog.discounts.application;

import com.perroamor.inventory.catalog.discounts.domain.CreateDiscountCommand;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
import com.perroamor.inventory.catalog.discounts.domain.DiscountRepository;
import com.perroamor.inventory.catalog.discounts.domain.SlotType;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DiscountService total-validation tests against a hand-written stub
 * DiscountRepository — no Spring context, no Mockito.
 */
class DiscountServiceTest {

    private StubDiscountRepository repository;
    private DiscountService service;

    @BeforeEach
    void setUp() {
        repository = new StubDiscountRepository();
        service = new DiscountService(repository);
    }

    @Test
    void uniformPriceAcrossOptionsAndMatchingTotalPersists() {
        CreateDiscountCommand command = new CreateDiscountCommand(
                "Combo A",
                "desc",
                new BigDecimal("130.00"),
                List.of(
                        new CreateDiscountCommand.NewSlot(0, SlotType.FIXED, 2,
                                List.of(new CreateDiscountCommand.NewOption(1L, new BigDecimal("50.00")))),
                        new CreateDiscountCommand.NewSlot(1, SlotType.GROUP, 1,
                                List.of(
                                        new CreateDiscountCommand.NewOption(2L, new BigDecimal("30.00")),
                                        new CreateDiscountCommand.NewOption(3L, new BigDecimal("30.00"))))));

        Discount created = service.create(command);

        assertThat(created).isNotNull();
        assertThat(repository.savedDiscounts).hasSize(1);
        assertThat(repository.savedDiscounts.get(0).totalPrice()).isEqualByComparingTo("130.00");
    }

    @Test
    void perSlotPriceMismatchIsRejectedAndPersistsNothing() {
        CreateDiscountCommand command = new CreateDiscountCommand(
                "Combo A",
                "desc",
                new BigDecimal("130.00"),
                List.of(
                        new CreateDiscountCommand.NewSlot(0, SlotType.FIXED, 2,
                                List.of(new CreateDiscountCommand.NewOption(1L, new BigDecimal("50.00")))),
                        // Group slot options with DIFFERENT prices -- violates per-slot uniformity.
                        new CreateDiscountCommand.NewSlot(1, SlotType.GROUP, 1,
                                List.of(
                                        new CreateDiscountCommand.NewOption(2L, new BigDecimal("30.00")),
                                        new CreateDiscountCommand.NewOption(3L, new BigDecimal("35.00"))))));

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(ValidationException.class);
        assertThat(repository.savedDiscounts).isEmpty();
    }

    @Test
    void declaredTotalMismatchIsRejectedAndPersistsNothing() {
        CreateDiscountCommand command = new CreateDiscountCommand(
                "Combo A",
                "desc",
                new BigDecimal("999.00"), // does not match slot sum (100 + 30 = 130)
                List.of(
                        new CreateDiscountCommand.NewSlot(0, SlotType.FIXED, 2,
                                List.of(new CreateDiscountCommand.NewOption(1L, new BigDecimal("50.00")))),
                        new CreateDiscountCommand.NewSlot(1, SlotType.GROUP, 1,
                                List.of(new CreateDiscountCommand.NewOption(2L, new BigDecimal("30.00"))))));

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(ValidationException.class);
        assertThat(repository.savedDiscounts).isEmpty();
    }

    private static final class StubDiscountRepository implements DiscountRepository {
        private final List<Discount> savedDiscounts = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Page<Discount> search(DiscountFilter filter, PageRequest pageRequest) {
            return Page.of(savedDiscounts, pageRequest.page(), pageRequest.size(), savedDiscounts.size());
        }

        @Override
        public Optional<Discount> findById(Long id) {
            return savedDiscounts.stream().filter(d -> d.id().equals(id)).findFirst();
        }

        @Override
        public List<Discount> findActive() {
            return savedDiscounts.stream().filter(Discount::isActive).toList();
        }

        @Override
        public Discount save(Discount discount) {
            Discount saved = new Discount(nextId++, discount.name(), discount.description(),
                    discount.totalPrice(), discount.isActive(), discount.createdAt(), discount.slots());
            savedDiscounts.add(saved);
            return saved;
        }

        @Override
        public Discount replace(Discount discount) {
            savedDiscounts.removeIf(d -> d.id().equals(discount.id()));
            savedDiscounts.add(discount);
            return discount;
        }

        @Override
        public void softDelete(Long id) {
            savedDiscounts.removeIf(d -> d.id().equals(id));
        }
    }
}
