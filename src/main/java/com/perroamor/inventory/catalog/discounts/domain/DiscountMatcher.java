package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure, Spring-free sale-time discount matcher. Given a cart (available quantity
 * per product id) and the list of active discounts, finds the discount whose
 * required products are an EXACT match for the distinct products in the cart --
 * not a partial/subset match -- and reports how many times it can be applied
 * plus the total quantity/price consumed per product. The business guarantees
 * discount configs are unique per exact product set, so at most one discount is
 * expected to match a given cart; list order is only a tie-break for the
 * (otherwise not expected) case of two discounts requiring the identical set.
 *
 * "Exact" is about which DISTINCT products are present, not their quantity: a
 * cart can still have more units of an already-required product than one
 * application needs (see buildResult) -- that's still an exact match, just
 * applied N times with any leftover units of that product priced normally.
 * What disqualifies a discount is the cart containing a product the discount
 * doesn't require at all.
 *
 * Search: bounded backtracking over each slot's options (depth = slot count,
 * branch = option count per slot). Required for group slots: greedy first-fit
 * per slot produces false negatives when the same product is a valid option in
 * two different group slots (see design.md "Matching search" decision).
 */
public final class DiscountMatcher {

    private DiscountMatcher() {
    }

    public static Optional<MatchResult> match(Map<Long, Integer> availableByProduct, List<Discount> active) {
        for (Discount discount : active) {
            Optional<MatchResult> result = matchDiscount(availableByProduct, discount);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static Optional<MatchResult> matchDiscount(Map<Long, Integer> availableByProduct, Discount discount) {
        List<DiscountSlot> slots = discount.slots();
        DiscountSlotOption[] chosen = new DiscountSlotOption[slots.size()];
        return backtrack(availableByProduct, discount, slots, 0, chosen);
    }

    private static Optional<MatchResult> backtrack(Map<Long, Integer> availableByProduct,
                                                     Discount discount,
                                                     List<DiscountSlot> slots,
                                                     int index,
                                                     DiscountSlotOption[] chosen) {
        if (index == slots.size()) {
            return buildResult(availableByProduct, discount, slots, chosen);
        }
        for (DiscountSlotOption option : slots.get(index).options()) {
            chosen[index] = option;
            Optional<MatchResult> result = backtrack(availableByProduct, discount, slots, index + 1, chosen);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static Optional<MatchResult> buildResult(Map<Long, Integer> availableByProduct,
                                                       Discount discount,
                                                       List<DiscountSlot> slots,
                                                       DiscountSlotOption[] chosen) {
        Map<Long, Integer> requiredPerApplication = new HashMap<>();
        Map<Long, BigDecimal> priceByProduct = new HashMap<>();
        for (int i = 0; i < slots.size(); i++) {
            DiscountSlotOption option = chosen[i];
            requiredPerApplication.merge(option.productId(), slots.get(i).quantity(), Integer::sum);
            priceByProduct.put(option.productId(), option.finalUnitPrice());
        }

        // Exact match: the cart's distinct products must be precisely the ones this
        // discount requires -- a product in the cart that this discount doesn't
        // touch at all disqualifies it, even if every slot could otherwise be filled.
        Set<Long> cartProducts = availableByProduct.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (!cartProducts.equals(requiredPerApplication.keySet())) {
            return Optional.empty();
        }

        int applications = Integer.MAX_VALUE;
        for (Map.Entry<Long, Integer> entry : requiredPerApplication.entrySet()) {
            int available = availableByProduct.getOrDefault(entry.getKey(), 0);
            int possible = available / entry.getValue();
            applications = Math.min(applications, possible);
        }
        if (applications <= 0) {
            return Optional.empty();
        }

        Map<Long, MatchResult.Consumed> consumedByProduct = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : requiredPerApplication.entrySet()) {
            Long productId = entry.getKey();
            int totalQty = entry.getValue() * applications;
            consumedByProduct.put(productId, new MatchResult.Consumed(totalQty, priceByProduct.get(productId)));
        }

        return Optional.of(new MatchResult(discount.id(), discount.name(), applications, consumedByProduct));
    }

    public record MatchResult(Long discountId, String discountName, int applications,
                               Map<Long, Consumed> consumedByProduct) {
        public record Consumed(int quantity, BigDecimal finalUnitPrice) {
        }
    }
}
