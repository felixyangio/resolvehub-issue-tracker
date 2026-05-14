package com.resolvehub.dto.response;

import com.resolvehub.entity.AuditLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AuditLogResponse {

    private final UUID id;
    private final String action;
    private final String oldValue;
    private final String newValue;
    private final String message;
    private final UUID actorId;
    private final String actorName;
    private final String actorRole;
    private final LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction().name())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .message(auditLog.getMessage())
                .actorId(auditLog.getActor().getId())
                .actorName(auditLog.getActor().getName())
                .actorRole(auditLog.getActor().getRole().name())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
