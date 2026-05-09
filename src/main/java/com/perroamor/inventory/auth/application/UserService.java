package com.perroamor.inventory.auth.application;

import com.perroamor.inventory.auth.domain.Role;
import com.perroamor.inventory.auth.domain.RoleName;
import com.perroamor.inventory.auth.domain.RoleRepository;
import com.perroamor.inventory.auth.domain.User;
import com.perroamor.inventory.auth.domain.UserRepository;
import com.perroamor.inventory.shared.error.BusinessRuleException;
import com.perroamor.inventory.shared.error.ConflictException;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list(boolean includeInactive) {
        return userRepository.findAll(includeInactive);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Usuario", id));
    }

    public User create(String username, String email, String fullName, String plainPassword, RoleName role) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new ConflictException("Ya existe un usuario con el username '" + username + "'.");
        });
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new ConflictException("Ya existe un usuario con el email '" + email + "'.");
        });

        Role resolvedRole = roleRepository.findByName(role)
                .orElseThrow(() -> NotFoundException.of("Rol", role.name()));

        User toSave = new User(
                null,
                username,
                passwordEncoder.encode(plainPassword),
                email,
                fullName,
                resolvedRole.id(),
                role,
                true,
                null,
                null);
        return userRepository.save(toSave);
    }

    public User update(Long id,
                       String username,
                       String email,
                       String fullName,
                       String plainPasswordOrNull,
                       RoleName role,
                       boolean isActive,
                       Authentication currentAuth) {
        User existing = getById(id);

        if (isCurrentUser(existing, currentAuth) && !isActive) {
            throw new BusinessRuleException("No podés desactivar tu propia cuenta.");
        }

        userRepository.findByUsername(username).ifPresent(other -> {
            if (!other.id().equals(id)) {
                throw new ConflictException("Ya existe otro usuario con el username '" + username + "'.");
            }
        });
        userRepository.findByEmail(email).ifPresent(other -> {
            if (!other.id().equals(id)) {
                throw new ConflictException("Ya existe otro usuario con el email '" + email + "'.");
            }
        });

        Role resolvedRole = roleRepository.findByName(role)
                .orElseThrow(() -> NotFoundException.of("Rol", role.name()));

        String passwordHash = (plainPasswordOrNull != null && !plainPasswordOrNull.isBlank())
                ? passwordEncoder.encode(plainPasswordOrNull)
                : null;

        User updated = new User(
                existing.id(),
                username,
                passwordHash,
                email,
                fullName,
                resolvedRole.id(),
                role,
                isActive,
                existing.createdAt(),
                existing.lastLogin());
        return userRepository.update(updated);
    }

    public void delete(Long id, Authentication currentAuth) {
        User existing = getById(id);
        if (isCurrentUser(existing, currentAuth)) {
            throw new BusinessRuleException("No podés eliminar tu propia cuenta.");
        }
        userRepository.softDelete(id);
    }

    private boolean isCurrentUser(User user, Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String currentUsername = jwtAuth.getToken().getSubject();
            return user.username().equals(currentUsername);
        }
        return false;
    }
}
