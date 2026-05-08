package com.perroamor.inventory.auth.application;

import com.perroamor.inventory.auth.domain.User;
import com.perroamor.inventory.auth.infrastructure.security.JwtService;

public record AuthSession(JwtService.TokenPair tokens, User user) {
}
