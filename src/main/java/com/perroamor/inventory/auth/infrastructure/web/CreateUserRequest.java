package com.perroamor.inventory.auth.infrastructure.web;

import com.perroamor.inventory.auth.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull RoleName role
) {
}
