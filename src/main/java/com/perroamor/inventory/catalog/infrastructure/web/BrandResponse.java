package com.perroamor.inventory.catalog.infrastructure.web;

import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String name,
        String description,
        String baseColor,
        boolean isActive,
        LocalDateTime createdAt
) {
}
