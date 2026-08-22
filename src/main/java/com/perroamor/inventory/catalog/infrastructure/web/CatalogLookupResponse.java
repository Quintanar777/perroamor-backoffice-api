package com.perroamor.inventory.catalog.infrastructure.web;

public record CatalogLookupResponse(
        String matchType,
        ProductResponse product,
        ProductVariantResponse variant
) {
}
