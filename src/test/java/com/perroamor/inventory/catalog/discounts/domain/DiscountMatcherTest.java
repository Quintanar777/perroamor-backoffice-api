package com.perroamor.inventory.catalog.discounts.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link DiscountMatcher} — no Spring context.
 * Scenarios mirror product-discounts spec.md: one combination, two combinations,
 * incomplete cart, overlapping discounts (first wins), group-slot backtracking.
 */
class DiscountMatcherTest {

    private static final Long PRODUCT_A = 1L;
    private static final Long PRODUCT_B = 2L;
    private static final Long PRODUCT_C = 3L;
    private static final Long PRODUCT_D = 4L;

    @Test
    void oneCompleteCombinationRepricesLines() {
        Discount discount = discount(10L, "Fixed + Group",
                fixedSlot(0, PRODUCT_A, 2, "50.00"),
                groupSlot(1, option(PRODUCT_B, "30.00"), option(PRODUCT_C, "35.00")));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 2, PRODUCT_B, 1);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(discount));

        assertThat(result).isPresent();
        DiscountMatcher.MatchResult match = result.get();
        assertThat(match.discountId()).isEqualTo(10L);
        assertThat(match.applications()).isEqualTo(1);
        assertThat(match.consumedByProduct().get(PRODUCT_A))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(2, new BigDecimal("50.00")));
        assertThat(match.consumedByProduct().get(PRODUCT_B))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(1, new BigDecimal("30.00")));
        assertThat(match.consumedByProduct()).doesNotContainKey(PRODUCT_C);
    }

    @Test
    void twoCompleteCombinationsApplyTwice() {
        Discount discount = discount(10L, "Fixed + Group",
                fixedSlot(0, PRODUCT_A, 2, "50.00"),
                groupSlot(1, option(PRODUCT_B, "30.00"), option(PRODUCT_C, "35.00")));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 4, PRODUCT_B, 2);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(discount));

        assertThat(result).isPresent();
        DiscountMatcher.MatchResult match = result.get();
        assertThat(match.applications()).isEqualTo(2);
        assertThat(match.consumedByProduct().get(PRODUCT_A))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(4, new BigDecimal("50.00")));
        assertThat(match.consumedByProduct().get(PRODUCT_B))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(2, new BigDecimal("30.00")));
    }

    @Test
    void incompleteCombinationIsIgnored() {
        Discount discount = discount(10L, "Fixed + Group",
                fixedSlot(0, PRODUCT_A, 2, "50.00"),
                groupSlot(1, option(PRODUCT_B, "30.00"), option(PRODUCT_C, "35.00")));

        // Missing product B/C entirely — slot 1 cannot be satisfied.
        Map<Long, Integer> cart = Map.of(PRODUCT_A, 2);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(discount));

        assertThat(result).isEmpty();
    }

    @Test
    void overlappingEligibilityAppliesOnlyFirstDiscount() {
        Discount first = discount(10L, "First", fixedSlot(0, PRODUCT_A, 1, "20.00"));
        Discount second = discount(20L, "Second", fixedSlot(0, PRODUCT_A, 1, "22.00"));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 1);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(first, second));

        assertThat(result).isPresent();
        assertThat(result.get().discountId()).isEqualTo(10L);
    }

    @Test
    void groupSlotBacktrackingFindsFeasibleAssignmentWhenGreedyFirstChoiceFails() {
        // Both group slots list product C first. Greedy first-fit would pick C for
        // both slots and fail (only 1 unit of C available). Backtracking must
        // retry slot 1 with product B to find the feasible assignment.
        Discount discount = discount(10L, "Overlapping groups",
                groupSlot(0, option(PRODUCT_C, "40.00"), option(PRODUCT_B, "45.00")),
                groupSlot(1, option(PRODUCT_C, "40.00"), option(PRODUCT_D, "42.00")));

        Map<Long, Integer> cart = Map.of(PRODUCT_C, 1, PRODUCT_B, 1, PRODUCT_D, 0);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(discount));

        assertThat(result).isPresent();
        DiscountMatcher.MatchResult match = result.get();
        assertThat(match.applications()).isEqualTo(1);
        assertThat(match.consumedByProduct().get(PRODUCT_B))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(1, new BigDecimal("45.00")));
        assertThat(match.consumedByProduct().get(PRODUCT_C))
                .isEqualTo(new DiscountMatcher.MatchResult.Consumed(1, new BigDecimal("40.00")));
        assertThat(match.consumedByProduct()).doesNotContainKey(PRODUCT_D);
    }

    private static Discount discount(Long id, String name, DiscountSlot... slots) {
        return new Discount(id, name, null, BigDecimal.ZERO, true, null, List.of(slots));
    }

    private static DiscountSlot fixedSlot(int position, Long productId, int quantity, String price) {
        return new DiscountSlot(null, position, SlotType.FIXED, quantity,
                List.of(option(productId, price)));
    }

    private static DiscountSlot groupSlot(int position, DiscountSlotOption... options) {
        return new DiscountSlot(null, position, SlotType.GROUP, 1, List.of(options));
    }

    private static DiscountSlotOption option(Long productId, String price) {
        return new DiscountSlotOption(null, productId, null, new BigDecimal(price));
    }
}
