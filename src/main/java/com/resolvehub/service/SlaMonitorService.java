package com.resolvehub.service;

import com.resolvehub.entity.Incident;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.event.IncidentEvent;
import com.resolvehub.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service that monitors SLA compliance.
 *
 * <p>Runs every 15 minutes to check for incidents whose {@code dueAt}
 * has passed while still in an active status (NEW, ASSIGNED, IN_PROGRESS).
 * Breached cases are logged and notification events are published.</p>
 *
 * <p><strong>Interview note:</strong> Demonstrates @Scheduled with cron
 * expressions, transactional batch processing, and event-driven
 * notifications for operational alerts.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlaMonitorService {

    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Check for SLA breaches every 15 minutes.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional(readOnly = true)
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<Incident> breached = incidentRepository.findOverdueIncidents(now);

        if (breached.isEmpty()) {
            log.debug("SLA check: no breached incidents found");
            return;
        }

        log.warn("SLA check: {} breached incident(s) found", breached.size());

        for (Incident incident : breached) {
            // Notify the assigned agent (or creator if unassigned)
            String recipientEmail;
            String recipientName;
            if (incident.getAssignedTo() != null) {
                recipientEmail = incident.getAssignedTo().getEmail();
                recipientName = incident.getAssignedTo().getName();
            } else {
                recipientEmail = incident.getCreatedBy().getEmail();
                recipientName = incident.getCreatedBy().getName();
            }

            eventPublisher.publishEvent(new IncidentEvent(
                    this, IncidentEvent.Type.SLA_BREACHED,
                    incident.getId(), incident.getTitle(),
                    recipientEmail, recipientName,
                    "Due at: " + incident.getDueAt() + ", Current status: " + incident.getStatus()));

            log.warn("SLA breached — Case [{}] '{}' was due at {} (status: {})",
                    incident.getId(), incident.getTitle(),
                    incident.getDueAt(), incident.getStatus());
        }
    }
}
