package com.resolvehub.service;

import com.resolvehub.dto.request.AssignIncidentRequest;
import com.resolvehub.dto.request.CreateIncidentRequest;
import com.resolvehub.dto.request.UpdateIncidentRequest;
import com.resolvehub.dto.request.UpdateStatusRequest;
import com.resolvehub.dto.response.IncidentResponse;
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
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private StatusTransitionValidator statusTransitionValidator = new StatusTransitionValidator();

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private IncidentClassificationService classificationService;

    @Mock
    private CategoryClassificationService categoryClassificationService;

    @InjectMocks
    private IncidentService incidentService;

    private User userAlice;
    private User agentBob;
    private User managerCarol;
    private CustomUserDetails aliceDetails;
    private CustomUserDetails bobDetails;
    private CustomUserDetails carolDetails;

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

        managerCarol = new User();
        managerCarol.setId(UUID.randomUUID());
        managerCarol.setName("Carol");
        managerCarol.setEmail("carol@example.com");
        managerCarol.setRole(Role.MANAGER);
        managerCarol.setEnabled(true);

        aliceDetails = new CustomUserDetails(userAlice);
        bobDetails = new CustomUserDetails(agentBob);
        carolDetails = new CustomUserDetails(managerCarol);
    }

    private Incident buildIncident(User creator, IncidentStatus status, User assignedTo) {
        Incident incident = new Incident();
        incident.setId(UUID.randomUUID());
        incident.setTitle("Test incident");
        incident.setDescription("Test description");
        incident.setCategory(IncidentCategory.MAINTENANCE);
        incident.setPriority(Priority.MEDIUM);
        incident.setStatus(status);
        incident.setCreatedBy(creator);
        incident.setAssignedTo(assignedTo);
        return incident;
    }

    // --- Create ---

    @Nested
    class CreateTests {

        @Test
        void shouldCreateIncidentWithStatusNew() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
            when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
                Incident saved = invocation.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            when(classificationService.classify(anyString(), anyString(), any(IncidentCategory.class)))
                    .thenReturn(Priority.MEDIUM);

            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setTitle("Server down");
            request.setDescription("API not responding");
            request.setCategory(IncidentCategory.MAINTENANCE);

            IncidentResponse response = incidentService.create(request, aliceDetails);

            assertNotNull(response);
            assertEquals("NEW", response.getStatus());
            assertEquals("MEDIUM", response.getPriority());
        }
    }

    // --- Read Access ---

    @Nested
    class ReadAccessTests {

        @Test
        void userCanViewOwnIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            IncidentResponse response = incidentService.findById(incident.getId(), aliceDetails);
            assertEquals(incident.getId(), response.getId());
        }

        @Test
        void userCannotViewOtherUsersIncident() {
            Incident incident = buildIncident(managerCarol, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.findById(incident.getId(), aliceDetails));
        }

        @Test
        void agentCanViewAssignedIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.ASSIGNED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            IncidentResponse response = incidentService.findById(incident.getId(), bobDetails);
            assertEquals(incident.getId(), response.getId());
        }

        @Test
        void agentCannotViewUnassignedIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.findById(incident.getId(), bobDetails));
        }

        @Test
        void managerCanViewAnyIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            IncidentResponse response = incidentService.findById(incident.getId(), carolDetails);
            assertEquals(incident.getId(), response.getId());
        }
    }

    // --- Write Access ---

    @Nested
    class WriteAccessTests {

        @Test
        void userCanUpdateOwnNewIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
            when(incidentRepository.save(any())).thenReturn(incident);

            UpdateIncidentRequest request = new UpdateIncidentRequest();
            request.setTitle("Updated title");

            IncidentResponse response = incidentService.update(incident.getId(), request, aliceDetails);
            assertEquals("Updated title", response.getTitle());
        }

        @Test
        void userCannotUpdateResolvedIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.RESOLVED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            UpdateIncidentRequest request = new UpdateIncidentRequest();
            request.setTitle("Updated title");

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.update(incident.getId(), request, aliceDetails));
        }

        @Test
        void userCannotUpdateClosedIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.CLOSED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            UpdateIncidentRequest request = new UpdateIncidentRequest();
            request.setTitle("Updated title");

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.update(incident.getId(), request, aliceDetails));
        }

        @Test
        void managerCanUpdateResolvedIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.RESOLVED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(incidentRepository.save(any())).thenReturn(incident);

            UpdateIncidentRequest request = new UpdateIncidentRequest();
            request.setPriority(Priority.CRITICAL);

            IncidentResponse response = incidentService.update(incident.getId(), request, carolDetails);
            assertNotNull(response);
        }
    }

    // --- Assignment ---

    @Nested
    class AssignmentTests {

        @Test
        void managerCanAssignIncidentToAgent() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(agentBob.getId())).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentBob.getId());

            IncidentResponse response = incidentService.assign(incident.getId(), request, carolDetails);

            assertEquals(agentBob.getId(), response.getAssignedToId());
            assertEquals("ASSIGNED", response.getStatus());
        }

        @Test
        void assigningNewIncidentAutoTransitionsToAssigned() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(agentBob.getId())).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentBob.getId());

            IncidentResponse response = incidentService.assign(incident.getId(), request, carolDetails);
            assertEquals("ASSIGNED", response.getStatus());
        }

        @Test
        void assigningInProgressIncidentKeepsStatus() {
            Incident incident = buildIncident(userAlice, IncidentStatus.IN_PROGRESS, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(agentBob.getId())).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentBob.getId());

            IncidentResponse response = incidentService.assign(incident.getId(), request, carolDetails);
            assertEquals("IN_PROGRESS", response.getStatus());
        }

        @Test
        void userCannotAssignIncident() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentBob.getId());

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.assign(incident.getId(), request, aliceDetails));
        }

        @Test
        void cannotAssignToNonAgent() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(userAlice.getId())).thenReturn(Optional.of(userAlice));

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(userAlice.getId());

            assertThrows(BadRequestException.class,
                    () -> incidentService.assign(incident.getId(), request, carolDetails));
        }

        @Test
        void cannotAssignToNonexistentUser() {
            UUID fakeId = UUID.randomUUID();
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(fakeId)).thenReturn(Optional.empty());

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(fakeId);

            assertThrows(ResourceNotFoundException.class,
                    () -> incidentService.assign(incident.getId(), request, carolDetails));
        }
    }

    // --- Status Transitions ---

    @Nested
    class StatusTransitionTests {

        @Test
        void agentCanTransitionAssignedToInProgress() {
            Incident incident = buildIncident(userAlice, IncidentStatus.ASSIGNED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.IN_PROGRESS);

            IncidentResponse response = incidentService.updateStatus(incident.getId(), request, bobDetails);
            assertEquals("IN_PROGRESS", response.getStatus());
        }

        @Test
        void agentCannotTransitionNewToInProgress() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(agentBob));

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.IN_PROGRESS);

            assertThrows(BadRequestException.class,
                    () -> incidentService.updateStatus(incident.getId(), request, bobDetails));
        }

        @Test
        void userCannotChangeStatus() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.ASSIGNED);

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.updateStatus(incident.getId(), request, aliceDetails));
        }

        @Test
        void managerCanChangeAnyStatus() {
            Incident incident = buildIncident(userAlice, IncidentStatus.RESOLVED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.CLOSED);

            IncidentResponse response = incidentService.updateStatus(incident.getId(), request, carolDetails);
            assertEquals("CLOSED", response.getStatus());
        }
    }

    // --- Audit Log Verification ---

    @Nested
    class AuditLogTests {

        @Test
        void createIncidentLogsAuditEntry() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
            when(classificationService.classify(anyString(), anyString(), any(IncidentCategory.class)))
                    .thenReturn(Priority.MEDIUM);
            when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
                Incident saved = inv.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setTitle("Test");
            request.setDescription("Desc");
            request.setCategory(IncidentCategory.MAINTENANCE);

            incidentService.create(request, aliceDetails);

            verify(auditLogRepository).save(argThat(log ->
                    log.getAction() == AuditAction.INCIDENT_CREATED
                            && log.getActor().equals(userAlice)));
        }

        @Test
        void assignIncidentLogsAuditEntry() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
            when(userRepository.findById(agentBob.getId())).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentBob.getId());

            incidentService.assign(incident.getId(), request, carolDetails);

            verify(auditLogRepository).save(argThat(log ->
                    log.getAction() == AuditAction.INCIDENT_ASSIGNED
                            && "Bob".equals(log.getNewValue())));
        }

        @Test
        void statusChangeLogsAuditEntry() {
            Incident incident = buildIncident(userAlice, IncidentStatus.ASSIGNED, agentBob);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(agentBob));
            when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.IN_PROGRESS);

            incidentService.updateStatus(incident.getId(), request, bobDetails);

            verify(auditLogRepository).save(argThat(log ->
                    log.getAction() == AuditAction.STATUS_CHANGED
                            && "ASSIGNED".equals(log.getOldValue())
                            && "IN_PROGRESS".equals(log.getNewValue())));
        }
    }

    // --- Delete ---

    @Nested
    class DeleteTests {

        @Test
        void managerCanDelete() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);
            when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

            incidentService.delete(incident.getId(), carolDetails);
            verify(incidentRepository).delete(incident);
        }

        @Test
        void userCannotDelete() {
            Incident incident = buildIncident(userAlice, IncidentStatus.NEW, null);

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.delete(incident.getId(), aliceDetails));
        }

        @Test
        void agentCannotDelete() {
            Incident incident = buildIncident(userAlice, IncidentStatus.ASSIGNED, agentBob);

            assertThrows(AccessDeniedException.class,
                    () -> incidentService.delete(incident.getId(), bobDetails));
        }
    }
}
