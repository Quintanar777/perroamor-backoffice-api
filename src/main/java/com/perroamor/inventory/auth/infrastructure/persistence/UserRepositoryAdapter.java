package com.perroamor.inventory.auth.infrastructure.persistence;

import com.perroamor.inventory.auth.domain.User;
import com.perroamor.inventory.auth.domain.UserRepository;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final RoleJpaRepository roleJpa;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpa,
                                 RoleJpaRepository roleJpa,
                                 UserMapper mapper) {
        this.jpa = jpa;
        this.roleJpa = roleJpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll(boolean includeInactive) {
        List<UserJpaEntity> entities = includeInactive ? jpa.findAll() : jpa.findAllByIsActiveTrue();
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public User save(User user) {
        RoleJpaEntity roleRef = roleJpa.findById(user.roleId())
                .orElseThrow(() -> NotFoundException.of("Rol", user.roleId()));
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername(user.username());
        entity.setPasswordHash(user.passwordHash());
        entity.setEmail(user.email());
        entity.setFullName(user.fullName());
        entity.setRole(roleRef);
        entity.setActive(user.isActive());
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public User update(User user) {
        UserJpaEntity existing = jpa.findById(user.id())
                .orElseThrow(() -> NotFoundException.of("Usuario", user.id()));
        existing.setUsername(user.username());
        existing.setEmail(user.email());
        existing.setFullName(user.fullName());
        existing.setActive(user.isActive());

        if (user.passwordHash() != null) {
            existing.setPasswordHash(user.passwordHash());
        }
        if (existing.getRole() == null || !existing.getRole().getId().equals(user.roleId())) {
            RoleJpaEntity roleRef = roleJpa.findById(user.roleId())
                    .orElseThrow(() -> NotFoundException.of("Rol", user.roleId()));
            existing.setRole(roleRef);
        }
        return mapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId, LocalDateTime when) {
        jpa.updateLastLogin(userId, when);
    }
}
