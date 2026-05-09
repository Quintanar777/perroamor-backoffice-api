package com.perroamor.inventory.catalog.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 500) String description,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe tener formato hex #RRGGBB")
        String baseColor,
        Boolean isActive
) {
}
