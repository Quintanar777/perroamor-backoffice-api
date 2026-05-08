package com.perroamor.inventory.auth.domain;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String passwordHash,
        String email,
        String fullName,
        Long roleId,
        RoleName roleName,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime lastLogin
) {
}
