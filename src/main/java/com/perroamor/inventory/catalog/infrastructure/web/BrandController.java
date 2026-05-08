package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.application.BrandService;
import com.perroamor.inventory.catalog.domain.Brand;
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
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;
    private final CatalogMapper mapper;

    public BrandController(BrandService brandService, CatalogMapper mapper) {
        this.brandService = brandService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<BrandResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return brandService.list(includeInactive).stream()
                .map(mapper::toBrandResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public BrandResponse getById(@PathVariable Long id) {
        return mapper.toBrandResponse(brandService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public BrandResponse create(@Valid @RequestBody BrandRequest request) {
        Brand created = brandService.create(request.name(), request.description());
        return mapper.toBrandResponse(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        boolean isActive = request.isActive() == null || request.isActive();
        Brand updated = brandService.update(id, request.name(), request.description(), isActive);
        return mapper.toBrandResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }
}
