package com.perroamor.inventory.auth.domain;

import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findById(Long id);

    Optional<Role> findByName(RoleName name);
}
