package com.perroamor.inventory.auth.infrastructure.web;

import com.perroamor.inventory.auth.application.AuthSession;
import com.perroamor.inventory.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "role", source = "roleName")
    UserResponse toUserResponse(User user);

    default LoginResponse toLoginResponse(AuthSession session) {
        var tokens = session.tokens();
        return new LoginResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                toUserResponse(session.user()));
    }
}
