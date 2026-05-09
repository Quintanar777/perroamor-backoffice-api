package com.perroamor.inventory.auth.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAll(boolean includeInactive);

    User save(User user);

    User update(User user);

    void softDelete(Long id);

    void updateLastLogin(Long userId, LocalDateTime when);
}
