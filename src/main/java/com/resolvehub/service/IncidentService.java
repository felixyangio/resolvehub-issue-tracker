package com.resolvehub.service;

import com.resolvehub.dto.request.AssignIncidentRequest;
import com.resolvehub.dto.request.CreateIncidentRequest;
import com.resolvehub.dto.request.UpdateIncidentRequest;
import com.resolvehub.dto.request.UpdateStatusRequest;
import com.resolvehub.dto.response.IncidentResponse;
import com.resolvehub.entity.AuditLog;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.AuditAction;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.exception.BadRequestException;
import com.resolvehub.exception.ResourceNotFoundException;
import com.resolvehub.repository.AuditLogRepository;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.repository.IncidentSpecification;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.event.IncidentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final StatusTransitionValidator statusTransitionValidator;
    private final AuditLogRepository auditLogRepository;
    private final IncidentClassificationService classificationService;
    private final CategoryClassificationService categoryClassificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public IncidentResponse create(CreateIncidentRequest request, CustomUserDetails userDetails) {
        User creator = getUserOrThrow(userDetails.getUsername());

        IncidentCategory resolvedCategory = request.getCategory() != null
                ? request.getCategory()
                : categoryClassificationService.classify(request.getTitle(), request.getDescription());

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(resolvedCategory)
                .priority(request.getPriority() != null ? request.getPriority()
                        : classificationService.classify(request.getTitle(), request.getDescription(), resolvedCategory))
                .status(IncidentStatus.NEW)
                .createdBy(creator)
                .dueAt(request.getDueAt())
                .caseNumber(incidentRepository.nextCaseNumber())
                .build();

        Incident saved = incidentRepository.save(incident);

        logAudit(saved, creator, AuditAction.INCIDENT_CREATED, null, null, "Incident created");

        return IncidentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> findAll(
            CustomUserDetails userDetails,
            IncidentStatus status,
            IncidentCategory category,
            Priority priority,
            Pageable pageable
    ) {
        Role role = Role.valueOf(userDetails.getRole());

        Specification<Incident> spec = Specification.where(null);

        if (role == Role.USER) {
            spec = spec.and(IncidentSpecification.createdBy(userDetails.getId()));
        } else if (role == Role.AGENT) {
            spec = spec.and(IncidentSpecification.assignedTo(userDetails.getId()));
        }

        spec = spec
                .and(IncidentSpecification.hasStatus(status))
                .and(IncidentSpecification.hasCategory(category))
                .and(IncidentSpecification.hasPriority(priority));

        return incidentRepository.findAll(spec, pageable).map(IncidentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public IncidentResponse findById(UUID id, CustomUserDetails userDetails) {
        Incident incident = getIncidentOrThrow(id);
        checkReadAccess(incident, userDetails);
        return IncidentResponse.fromEntity(incident);
    }

    @Transactional
    public IncidentResponse update(UUID id, UpdateIncidentRequest request, CustomUserDetails userDetails) {
        Incident incident = getIncidentOrThrow(id);
        checkWriteAccess(incident, userDetails);

        User actor = getUserOrThrow(userDetails.getUsername());

        if (request.getTitle() != null) {
            incident.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            incident.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            incident.setPriority(request.getPriority());
        }
        if (request.getDueAt() != null) {
            incident.setDueAt(request.getDueAt());
        }

        Incident updated = incidentRepository.save(incident);

        logAudit(updated, actor, AuditAction.INCIDENT_UPDATED, null, null, "Incident details updated");

        return IncidentResponse.fromEntity(updated);
    }

    @Transactional
    public IncidentResponse assign(UUID id, AssignIncidentRequest request, CustomUserDetails userDetails) {
        requireRole(userDetails, Role.MANAGER, Role.ADMIN);

        Incident incident = getIncidentOrThrow(id);
        User actor = getUserOrThrow(userDetails.getUsername());

        String oldAgentName = incident.getAssignedTo() != null
                ? incident.getAssignedTo().getName() : "Unassigned";

        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAgentId()));

        if (agent.getRole() != Role.AGENT) {
            throw new BadRequestException("Incidents can only be assigned to users with AGENT role");
        }

        incident.setAssignedTo(agent);

        if (incident.getStatus() == IncidentStatus.NEW) {
            incident.setStatus(IncidentStatus.ASSIGNED);
        }

        Incident updated = incidentRepository.save(incident);

        logAudit(updated, actor, AuditAction.INCIDENT_ASSIGNED,
                oldAgentName, agent.getName(), "Assigned to " + agent.getName());

        // Notify the assigned agent via email (async)
        eventPublisher.publishEvent(new IncidentEvent(
                this, IncidentEvent.Type.ASSIGNED,
                updated.getId(), updated.getTitle(),
                agent.getEmail(), agent.getName(),
                actor.getName()));

        return IncidentResponse.fromEntity(updated);
    }

    @Transactional
    public IncidentResponse updateStatus(UUID id, UpdateStatusRequest request, CustomUserDetails userDetails) {
        Incident incident = getIncidentOrThrow(id);
        checkStatusUpdateAccess(incident, userDetails);

        User actor = getUserOrThrow(userDetails.getUsername());
        IncidentStatus oldStatus = incident.getStatus();

        statusTransitionValidator.validate(oldStatus, request.getStatus());

        incident.setStatus(request.getStatus());

        Incident updated = incidentRepository.save(incident);

        logAudit(updated, actor, AuditAction.STATUS_CHANGED,
                oldStatus.name(), request.getStatus().name(),
                "Status changed from " + oldStatus + " to " + request.getStatus());

        // Notify case creator about status change (async)
        eventPublisher.publishEvent(new IncidentEvent(
                this, IncidentEvent.Type.STATUS_CHANGED,
                updated.getId(), updated.getTitle(),
                updated.getCreatedBy().getEmail(), updated.getCreatedBy().getName(),
                oldStatus + " → " + request.getStatus()));

        return IncidentResponse.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, CustomUserDetails userDetails) {
        requireRole(userDetails, Role.MANAGER, Role.ADMIN);
        Incident incident = getIncidentOrThrow(id);
        incidentRepository.delete(incident);
    }

    // --- package-private access control helpers (reused by CommentService, AuditLogService) ---

    Incident getIncidentOrThrow(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
    }

    void checkReadAccess(Incident incident, CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        if (role == Role.MANAGER || role == Role.ADMIN) {
            return;
        }

        if (role == Role.USER && incident.getCreatedBy().getId().equals(userDetails.getId())) {
            return;
        }

        if (role == Role.AGENT && isAssignedTo(incident, userDetails)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to view this incident");
    }

    // --- private helpers ---

    private void checkWriteAccess(Incident incident, CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        if (role == Role.MANAGER || role == Role.ADMIN) {
            return;
        }

        if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.CLOSED) {
            throw new AccessDeniedException("Cannot modify a resolved or closed incident");
        }

        if (role == Role.USER && incident.getCreatedBy().getId().equals(userDetails.getId())) {
            return;
        }

        if (role == Role.AGENT && isAssignedTo(incident, userDetails)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to update this incident");
    }

    private void checkStatusUpdateAccess(Incident incident, CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        if (role == Role.MANAGER || role == Role.ADMIN) {
            return;
        }

        if (role == Role.AGENT && isAssignedTo(incident, userDetails)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to change this incident's status");
    }

    private boolean isAssignedTo(Incident incident, CustomUserDetails userDetails) {
        return incident.getAssignedTo() != null
                && incident.getAssignedTo().getId().equals(userDetails.getId());
    }

    private void requireRole(CustomUserDetails userDetails, Role... allowedRoles) {
        Role role = Role.valueOf(userDetails.getRole());
        for (Role allowed : allowedRoles) {
            if (role == allowed) {
                return;
            }
        }
        throw new AccessDeniedException("You do not have the required role for this action");
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void logAudit(Incident incident, User actor, AuditAction action,
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
}
