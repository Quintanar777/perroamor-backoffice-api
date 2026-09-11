package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.application.ProductVariantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * inventory-2-0: variant write paths are retired. Each CSV product+size row is
 * now its own independent {@code Product}; no new {@code ProductVariant} is
 * created. Read endpoints stay for the pre-existing (now inactive) variant
 * rows referenced by historical sales.
 */
@RestController
@RequestMapping("/api/v1")
public class ProductVariantController {

    private final ProductVariantService variantService;
    private final CatalogMapper mapper;

    public ProductVariantController(ProductVariantService variantService, CatalogMapper mapper) {
        this.variantService = variantService;
        this.mapper = mapper;
    }

    @GetMapping("/products/{productId}/variants")
    public List<ProductVariantResponse> listByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return variantService.listByProduct(productId, includeInactive).stream()
                .map(mapper::toVariantResponse)
                .toList();
    }

    @GetMapping("/variants/{id}")
    public ProductVariantResponse getById(@PathVariable Long id) {
        return mapper.toVariantResponse(variantService.getById(id));
    }
}
