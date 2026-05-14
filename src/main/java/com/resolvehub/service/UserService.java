package com.resolvehub.service;

import com.resolvehub.dto.response.UserResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.ResourceNotFoundException;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
