package com.perroamor.inventory.auth.domain;

import java.time.LocalDateTime;

public record Role(
        Long id,
        RoleName name,
        String description,
        boolean isActive,
        LocalDateTime createdAt
) {
}
