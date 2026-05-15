package com.perroamor.inventory.sales.domain;

import java.time.LocalDateTime;

public record SaleFilter(
        Long eventId,
        LocalDateTime from,
        LocalDateTime to,
        PaymentMethod paymentMethod,
        Boolean isCancelled  // null=all, false=only active, true=only cancelled
) {
}
