package com.resolvehub.service;

import com.resolvehub.dto.response.AuditLogResponse;
import com.resolvehub.entity.AuditLog;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.AuditAction;
import com.resolvehub.repository.AuditLogRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final IncidentService incidentService;

    @Transactional
    public void log(Incident incident, User actor, AuditAction action,
                    String oldValue, String newValue, String message) {
        AuditLog auditLog = AuditLog.builder()
                .incident(incident)
                .actor(actor)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .message(message)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(UUID incidentId, CustomUserDetails userDetails) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);

        return auditLogRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
