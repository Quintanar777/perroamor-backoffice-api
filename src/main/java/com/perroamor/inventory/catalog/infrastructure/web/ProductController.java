package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductFilter;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import com.perroamor.inventory.shared.types.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final CatalogMapper mapper;

    public ProductController(ProductService productService, CatalogMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @GetMapping
    public PagedResponse<ProductResponse> search(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductFilter filter = new ProductFilter(brandId, category, q, isActive);
        Page<Product> result = productService.search(filter, PageRequest.of(page, size));
        return PagedResponse.map(result, mapper::toProductResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return mapper.toProductResponse(productService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        Product domain = mapper.toDomain(request);
        return mapper.toProductResponse(productService.create(domain));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product domain = mapper.toDomain(request);
        return mapper.toProductResponse(productService.update(id, domain));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductResponse adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        Product updated = request.setTo() != null
                ? productService.setStock(id, request.setTo())
                : productService.adjustStock(id, request.delta());
        return mapper.toProductResponse(updated);
    }

    @PostMapping("/backfill-codes")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ProductResponse> backfillCodes() {
        return productService.backfillMissingCodes().stream().map(mapper::toProductResponse).toList();
    }
}
