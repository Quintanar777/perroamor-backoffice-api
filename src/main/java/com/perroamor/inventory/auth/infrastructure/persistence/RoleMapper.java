package com.perroamor.inventory.auth.infrastructure.persistence;

import com.perroamor.inventory.auth.domain.Role;
import com.perroamor.inventory.auth.domain.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "name", source = "name", qualifiedByName = "stringToRoleName")
    @Mapping(target = "isActive", source = "active")
    Role toDomain(RoleJpaEntity entity);

    @Named("stringToRoleName")
    default RoleName stringToRoleName(String value) {
        return value == null ? null : RoleName.valueOf(value);
    }
}
