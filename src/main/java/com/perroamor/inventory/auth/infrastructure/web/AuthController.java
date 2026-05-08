package com.perroamor.inventory.auth.infrastructure.web;

import com.perroamor.inventory.auth.application.AuthService;
import com.perroamor.inventory.auth.application.AuthSession;
import com.perroamor.inventory.auth.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;

    public AuthController(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = authService.login(request.username(), request.password());
        return authMapper.toLoginResponse(session);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        AuthSession session = authService.refresh(request.refreshToken());
        return authMapper.toLoginResponse(session);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = authService.getCurrentUser(authentication);
        return authMapper.toUserResponse(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // MVP: stateless. El cliente borra sus tokens. No hay revocación server-side.
    }
}
