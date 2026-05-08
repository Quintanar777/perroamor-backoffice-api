package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/products/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductVariantResponse create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request) {
        ProductVariant domain = mapper.toDomain(request);
        return mapper.toVariantResponse(variantService.create(productId, domain));
    }

    @PutMapping("/variants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductVariantResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {
        ProductVariant domain = mapper.toDomain(request);
        return mapper.toVariantResponse(variantService.update(id, domain));
    }

    @DeleteMapping("/variants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        variantService.delete(id);
    }
}
