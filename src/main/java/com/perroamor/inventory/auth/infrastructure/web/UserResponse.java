package com.perroamor.inventory.auth.infrastructure.web;

import com.perroamor.inventory.auth.domain.RoleName;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        RoleName role,
        boolean isActive
) {
}
