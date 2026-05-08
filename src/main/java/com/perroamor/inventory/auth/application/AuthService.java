package com.perroamor.inventory.auth.application;

import com.perroamor.inventory.auth.domain.User;
import com.perroamor.inventory.auth.domain.UserRepository;
import com.perroamor.inventory.auth.infrastructure.security.JwtService;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.error.ValidationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthSession login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            throw new ValidationException("Credenciales inválidas.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Credenciales inválidas."));

        userRepository.updateLastLogin(user.id(), LocalDateTime.now());

        JwtService.TokenPair tokens = jwtService.issueTokens(user);
        return new AuthSession(tokens, user);
    }

    public AuthSession refresh(String refreshToken) {
        Jwt jwt;
        try {
            jwt = jwtService.parseAndValidateRefresh(refreshToken);
        } catch (RuntimeException e) {
            throw new ValidationException("Refresh token inválido o expirado.");
        }

        Long userId = extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario asociado al token no existe."));

        if (!user.isActive()) {
            throw new ValidationException("Usuario inactivo.");
        }

        JwtService.TokenPair tokens = jwtService.issueTokens(user);
        return new AuthSession(tokens, user);
    }

    public User getCurrentUser(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new ValidationException("Sesión inválida.");
        }
        String username = jwtAuth.getToken().getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario actual no encontrado."));
    }

    private Long extractUserId(Jwt jwt) {
        Object raw = jwt.getClaim(JwtService.CLAIM_USER_ID);
        if (raw instanceof Number n) {
            return n.longValue();
        }
        throw new ValidationException("Refresh token sin identificador de usuario.");
    }
}
