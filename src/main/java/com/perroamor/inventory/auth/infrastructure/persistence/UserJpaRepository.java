package com.perroamor.inventory.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByUsername(String username);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.lastLogin = :when WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("when") LocalDateTime when);
}
