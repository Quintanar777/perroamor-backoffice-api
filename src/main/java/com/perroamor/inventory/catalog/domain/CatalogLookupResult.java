package com.perroamor.inventory.catalog.domain;

public record CatalogLookupResult(Product product, ProductVariant variant) {

    public static CatalogLookupResult ofProduct(Product product) {
        return new CatalogLookupResult(product, null);
    }

    public static CatalogLookupResult ofVariant(Product product, ProductVariant variant) {
        return new CatalogLookupResult(product, variant);
    }

    public boolean isVariantMatch() {
        return variant != null;
    }
}
