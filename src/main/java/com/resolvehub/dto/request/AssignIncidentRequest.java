package com.resolvehub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignIncidentRequest {

    @NotNull(message = "Agent ID is required")
    private UUID agentId;
}
