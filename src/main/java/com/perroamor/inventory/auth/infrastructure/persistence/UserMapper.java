package com.perroamor.inventory.auth.infrastructure.persistence;

import com.perroamor.inventory.auth.domain.RoleName;
import com.perroamor.inventory.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name", qualifiedByName = "stringToRoleName")
    @Mapping(target = "isActive", source = "active")
    User toDomain(UserJpaEntity entity);

    @Named("stringToRoleName")
    default RoleName stringToRoleName(String value) {
        return value == null ? null : RoleName.valueOf(value);
    }
}
