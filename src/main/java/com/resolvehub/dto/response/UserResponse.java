package com.resolvehub.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private final UUID id;
    private final String name;
    private final String email;
    private final String role;
    private final boolean enabled;
    private final LocalDateTime createdAt;
}
