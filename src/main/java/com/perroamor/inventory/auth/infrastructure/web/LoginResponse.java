package com.perroamor.inventory.auth.infrastructure.web;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponse user
) {
}
