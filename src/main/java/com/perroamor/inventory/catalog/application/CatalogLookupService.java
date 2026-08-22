package com.perroamor.inventory.catalog.application;

import com.perroamor.inventory.catalog.domain.CatalogLookupResult;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductRepository;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.catalog.domain.ProductVariantRepository;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CatalogLookupService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public CatalogLookupService(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public CatalogLookupResult lookupByCode(String code) {
        return productRepository.findByCode(code)
                .map(CatalogLookupResult::ofProduct)
                .or(() -> variantRepository.findBySku(code).map(this::toVariantResult))
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró ningún producto con el código '" + code + "'."));
    }

    private CatalogLookupResult toVariantResult(ProductVariant variant) {
        Product parent = productRepository.findById(variant.productId())
                .orElseThrow(() -> new IllegalStateException(
                        "Variante " + variant.id() + " referencia un producto inexistente " + variant.productId()));
        return CatalogLookupResult.ofVariant(parent, variant);
    }
}
