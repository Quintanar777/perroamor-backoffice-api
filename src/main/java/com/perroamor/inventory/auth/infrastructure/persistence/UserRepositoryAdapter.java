package com.perroamor.inventory.auth.infrastructure.persistence;

import com.perroamor.inventory.auth.domain.User;
import com.perroamor.inventory.auth.domain.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpa, UserMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId, LocalDateTime when) {
        jpa.updateLastLogin(userId, when);
    }
}
