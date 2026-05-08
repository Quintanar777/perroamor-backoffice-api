package com.perroamor.inventory.auth.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken
) {
}
