package com.perroamor.inventory.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        int accessTtlMinutes,
        int refreshTtlDays,
        String issuer
) {
}
