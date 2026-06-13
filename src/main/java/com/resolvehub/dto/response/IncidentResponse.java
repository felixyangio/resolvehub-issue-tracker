package com.resolvehub.dto.response;

import com.resolvehub.entity.Incident;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class IncidentResponse {

    private final UUID id;
    private final Long caseNumber;
    private final String title;
    private final String description;
    private final String category;
    private final String priority;
    private final String status;
    private final UUID createdById;
    private final String createdByName;
    private final UUID assignedToId;
    private final String assignedToName;
    private final String createdByRole;
    private final String assignedToRole;
    private final LocalDateTime dueAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static IncidentResponse fromEntity(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .caseNumber(incident.getCaseNumber())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory().name())
                .priority(incident.getPriority().name())
                .status(incident.getStatus().name())
                .createdById(incident.getCreatedBy().getId())
                .createdByName(incident.getCreatedBy().getName())
                .assignedToId(incident.getAssignedTo() != null ? incident.getAssignedTo().getId() : null)
                .assignedToName(incident.getAssignedTo() != null ? incident.getAssignedTo().getName() : null)
                .createdByRole(incident.getCreatedBy().getRole().name())
                .assignedToRole(incident.getAssignedTo() != null ? incident.getAssignedTo().getRole().name() : null)
                .dueAt(incident.getDueAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
