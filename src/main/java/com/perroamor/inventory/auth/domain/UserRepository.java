package com.perroamor.inventory.auth.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    void updateLastLogin(Long userId, LocalDateTime when);
}
