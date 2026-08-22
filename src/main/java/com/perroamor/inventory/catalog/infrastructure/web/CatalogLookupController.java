package com.perroamor.inventory.catalog.infrastructure.web;

import com.perroamor.inventory.catalog.application.CatalogLookupService;
import com.perroamor.inventory.catalog.domain.CatalogLookupResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@Validated
public class CatalogLookupController {

    private final CatalogLookupService lookupService;
    private final CatalogMapper mapper;

    public CatalogLookupController(CatalogLookupService lookupService, CatalogMapper mapper) {
        this.lookupService = lookupService;
        this.mapper = mapper;
    }

    @GetMapping("/lookup")
    public CatalogLookupResponse lookup(@RequestParam @NotBlank String code) {
        CatalogLookupResult result = lookupService.lookupByCode(code);
        return new CatalogLookupResponse(
                result.isVariantMatch() ? "VARIANT" : "PRODUCT",
                mapper.toProductResponse(result.product()),
                result.isVariantMatch() ? mapper.toVariantResponse(result.variant()) : null);
    }
}
