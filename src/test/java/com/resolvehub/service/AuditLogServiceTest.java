package com.resolvehub.service;

import com.resolvehub.dto.response.AuditLogResponse;
import com.resolvehub.entity.AuditLog;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.AuditAction;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.repository.AuditLogRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private IncidentService incidentService;

    @InjectMocks
    private AuditLogService auditLogService;

    private User userAlice;
    private User agentBob;
    private CustomUserDetails aliceDetails;
    private CustomUserDetails bobDetails;
    private Incident incident;

    @BeforeEach
    void setUp() {
        userAlice = new User();
        userAlice.setId(UUID.randomUUID());
        userAlice.setName("Alice");
        userAlice.setEmail("alice@example.com");
        userAlice.setRole(Role.USER);
        userAlice.setEnabled(true);

        agentBob = new User();
        agentBob.setId(UUID.randomUUID());
        agentBob.setName("Bob");
        agentBob.setEmail("bob@example.com");
        agentBob.setRole(Role.AGENT);
        agentBob.setEnabled(true);

        aliceDetails = new CustomUserDetails(userAlice);
        bobDetails = new CustomUserDetails(agentBob);

        incident = new Incident();
        incident.setId(UUID.randomUUID());
        incident.setTitle("Test incident");
        incident.setDescription("Test");
        incident.setCategory(IncidentCategory.TECHNICAL);
        incident.setPriority(Priority.MEDIUM);
        incident.setStatus(IncidentStatus.NEW);
        incident.setCreatedBy(userAlice);
    }

    @Test
    void logCreatesAuditLogEntry() {
        auditLogService.log(incident, userAlice, AuditAction.INCIDENT_CREATED,
                null, null, "Incident created");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(AuditAction.INCIDENT_CREATED, saved.getAction());
        assertEquals("Incident created", saved.getMessage());
        assertEquals(userAlice, saved.getActor());
        assertEquals(incident, saved.getIncident());
    }

    @Test
    void logStatusChangeRecordsOldAndNewValues() {
        auditLogService.log(incident, agentBob, AuditAction.STATUS_CHANGED,
                "ASSIGNED", "IN_PROGRESS", "Status changed from ASSIGNED to IN_PROGRESS");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("ASSIGNED", saved.getOldValue());
        assertEquals("IN_PROGRESS", saved.getNewValue());
    }

    @Test
    void ownerCanViewAuditLogs() {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .incident(incident)
                .actor(userAlice)
                .action(AuditAction.INCIDENT_CREATED)
                .message("Incident created")
                .build();

        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, aliceDetails);
        when(auditLogRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId()))
                .thenReturn(List.of(log));

        List<AuditLogResponse> responses = auditLogService.getAuditLogs(incident.getId(), aliceDetails);

        assertEquals(1, responses.size());
        assertEquals("INCIDENT_CREATED", responses.get(0).getAction());
    }

    @Test
    void unauthorizedUserCannotViewAuditLogs() {
        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doThrow(new AccessDeniedException("You do not have permission to view this incident"))
                .when(incidentService).checkReadAccess(incident, bobDetails);

        assertThrows(AccessDeniedException.class,
                () -> auditLogService.getAuditLogs(incident.getId(), bobDetails));
    }
}
