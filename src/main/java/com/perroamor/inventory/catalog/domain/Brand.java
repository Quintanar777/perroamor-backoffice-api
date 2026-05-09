package com.perroamor.inventory.catalog.domain;

import java.time.LocalDateTime;

public record Brand(
        Long id,
        String name,
        String description,
        String baseColor,
        boolean isActive,
        LocalDateTime createdAt
) {
}
