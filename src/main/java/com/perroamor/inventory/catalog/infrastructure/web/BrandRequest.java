package com.perroamor.inventory.catalog.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 500) String description,
        Boolean isActive
) {
}
