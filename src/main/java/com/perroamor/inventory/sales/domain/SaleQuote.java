package com.perroamor.inventory.sales.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de una cotización: mismo pricing/discount-matching que produciría createSale para el
 * mismo carrito, pero sin persistir. {@code items} reutiliza {@link SaleItem} — sus campos
 * {@code id}/{@code saleId} quedan siempre null porque nunca se guarda.
 */
public record SaleQuote(
        List<SaleItem> items,
        BigDecimal itemsTotal,
        Long discountId,
        String discountName
) {
}
