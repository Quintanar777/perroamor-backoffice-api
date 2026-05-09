package com.perroamor.inventory.auth.infrastructure.web;

import com.perroamor.inventory.auth.application.UserService;
import com.perroamor.inventory.auth.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final AuthMapper mapper;

    public UserController(UserService userService, AuthMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return userService.list(includeInactive).stream()
                .map(mapper::toUserResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return mapper.toUserResponse(userService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.create(
                request.username(),
                request.email(),
                request.fullName(),
                request.password(),
                request.role());
        return mapper.toUserResponse(created);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest request,
                               Authentication authentication) {
        boolean isActive = request.isActive() == null || request.isActive();
        User updated = userService.update(
                id,
                request.username(),
                request.email(),
                request.fullName(),
                request.password(),
                request.role(),
                isActive,
                authentication);
        return mapper.toUserResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        userService.delete(id, authentication);
    }
}
