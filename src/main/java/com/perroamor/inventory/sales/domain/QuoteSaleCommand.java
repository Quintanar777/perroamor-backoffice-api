package com.perroamor.inventory.sales.domain;

import java.util.List;

/**
 * Cotización de venta sin efectos secundarios: no crea Sale, no decrementa stock, no persiste
 * nada. Reutiliza {@link CreateSaleCommand.NewItem} para el shape de línea (productId, quantity,
 * unitPriceOverride, personalization); comboId/variantId se validan como null igual que en
 * createSale, ya que inventory-2-0 retiró esos write-paths de venta.
 */
public record QuoteSaleCommand(
        List<CreateSaleCommand.NewItem> items,
        boolean isWholesale
) {
}
