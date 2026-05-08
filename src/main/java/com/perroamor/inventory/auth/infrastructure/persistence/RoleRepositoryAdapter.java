package com.perroamor.inventory.auth.infrastructure.persistence;

import com.perroamor.inventory.auth.domain.Role;
import com.perroamor.inventory.auth.domain.RoleName;
import com.perroamor.inventory.auth.domain.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpa;
    private final RoleMapper mapper;

    public RoleRepositoryAdapter(RoleJpaRepository jpa, RoleMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return jpa.findByName(name.name()).map(mapper::toDomain);
    }
}
