package com.resolvehub.controller;

import com.resolvehub.dto.request.AdminCreateUserRequest;
import com.resolvehub.dto.request.AdminUpdateUserRequest;
import com.resolvehub.dto.response.UserResponse;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public Page<UserResponse> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return userService.listAllUsers(pageable);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid AdminCreateUserRequest request) {
        return userService.adminCreateUser(request);
    }

    @PatchMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @RequestBody AdminUpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return userService.adminUpdateUser(id, request, principal.getId());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        userService.adminDeleteUser(id, principal.getId());
    }
}
