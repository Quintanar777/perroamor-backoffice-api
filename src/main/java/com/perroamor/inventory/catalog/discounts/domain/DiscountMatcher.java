package com.perroamor.inventory.catalog.discounts.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure, Spring-free sale-time discount matcher. Given a cart (available quantity
 * per product id) and the list of active discounts, finds the first discount
 * (in list order) whose slots can be fully satisfied at least once, and reports
 * how many times it can be applied plus the total quantity/price consumed per
 * product.
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
