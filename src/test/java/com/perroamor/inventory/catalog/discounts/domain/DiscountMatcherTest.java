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
 * incomplete cart, exact-cart-vs-discount matching (no partial-subset wins),
 * identical-requirement tie-break, group-slot backtracking.
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
    void identicalRequirementsTieBreakToFirstInListOrder() {
        // Both discounts require exactly the same cart (A:1) -- a config the business
        // guarantees won't happen for distinct carts, but if it ever does, list order
        // is the tie-break.
        Discount first = discount(10L, "First", fixedSlot(0, PRODUCT_A, 1, "20.00"));
        Discount second = discount(20L, "Second", fixedSlot(0, PRODUCT_A, 1, "22.00"));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 1);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(first, second));

        assertThat(result).isPresent();
        assertThat(result.get().discountId()).isEqualTo(10L);
    }

    @Test
    void exactCartMatchPicksDiscountCoveringWholeCartOverSmallerOverlappingOne() {
        // "b" needs A,B,C. "a" needs A,B,C,D. Cart has all four -- "b" is only a
        // subset of the cart (extra product D is unaccounted for), so it must NOT
        // match; "a" is the exact match for the whole cart and must be chosen, even
        // though "b" is listed first (reproduces the reported bug: adding the 4th
        // product must stop "b" from winning).
        Discount smaller = discount(20L, "b",
                fixedSlot(0, PRODUCT_A, 1, "10.00"),
                fixedSlot(1, PRODUCT_B, 1, "10.00"),
                fixedSlot(2, PRODUCT_C, 1, "10.00"));
        Discount larger = discount(10L, "a",
                fixedSlot(0, PRODUCT_A, 1, "8.00"),
                fixedSlot(1, PRODUCT_B, 1, "8.00"),
                fixedSlot(2, PRODUCT_C, 1, "8.00"),
                fixedSlot(3, PRODUCT_D, 1, "8.00"));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 1, PRODUCT_B, 1, PRODUCT_C, 1, PRODUCT_D, 1);

        Optional<DiscountMatcher.MatchResult> result =
                DiscountMatcher.match(cart, List.of(smaller, larger));

        assertThat(result).isPresent();
        assertThat(result.get().discountId()).isEqualTo(10L);
    }

    @Test
    void exactCartMatchStillPicksSmallerDiscountWhenCartMatchesItExactly() {
        // Same two discounts as above, but the cart only has the 3 products "b"
        // needs -- "a" can't match (missing D), so "b" must win.
        Discount smaller = discount(20L, "b",
                fixedSlot(0, PRODUCT_A, 1, "10.00"),
                fixedSlot(1, PRODUCT_B, 1, "10.00"),
                fixedSlot(2, PRODUCT_C, 1, "10.00"));
        Discount larger = discount(10L, "a",
                fixedSlot(0, PRODUCT_A, 1, "8.00"),
                fixedSlot(1, PRODUCT_B, 1, "8.00"),
                fixedSlot(2, PRODUCT_C, 1, "8.00"),
                fixedSlot(3, PRODUCT_D, 1, "8.00"));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 1, PRODUCT_B, 1, PRODUCT_C, 1);

        Optional<DiscountMatcher.MatchResult> result =
                DiscountMatcher.match(cart, List.of(smaller, larger));

        assertThat(result).isPresent();
        assertThat(result.get().discountId()).isEqualTo(20L);
    }

    @Test
    void discountIsIgnoredWhenCartHasAnUnrelatedExtraProduct() {
        // Cart has an extra product that isn't part of the discount at all (not a
        // surplus of an already-required product) -- must not match.
        Discount discount = discount(10L, "Solo A", fixedSlot(0, PRODUCT_A, 1, "20.00"));

        Map<Long, Integer> cart = Map.of(PRODUCT_A, 1, PRODUCT_B, 1);

        Optional<DiscountMatcher.MatchResult> result = DiscountMatcher.match(cart, List.of(discount));

        assertThat(result).isEmpty();
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
