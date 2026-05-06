package com.resolvehub.dto.request;

import com.resolvehub.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private IncidentStatus status;
}
