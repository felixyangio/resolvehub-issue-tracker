package com.resolvehub.service;

import com.resolvehub.dto.request.AdminCreateUserRequest;
import com.resolvehub.dto.request.AdminUpdateUserRequest;
import com.resolvehub.dto.response.UserResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.BadRequestException;
import com.resolvehub.exception.DuplicateResourceException;
import com.resolvehub.exception.ResourceNotFoundException;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser(CustomUserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<UserResponse> getAgents() {
        return userRepository.findByRoleAndEnabledTrue(Role.AGENT).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .enabled(u.isEnabled())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }

    // --- Admin operations ---

    @Transactional(readOnly = true)
    public Page<UserResponse> listAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(u -> UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .build());
    }

    @Transactional
    public UserResponse adminCreateUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse adminUpdateUser(UUID id, AdminUpdateUserRequest request, UUID callerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getId().equals(callerId)) {
            throw new BadRequestException("Admins cannot modify their own account here");
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void adminDeleteUser(UUID id, UUID callerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getId().equals(callerId)) {
            throw new BadRequestException("Admins cannot delete their own account");
        }
        userRepository.delete(user);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
