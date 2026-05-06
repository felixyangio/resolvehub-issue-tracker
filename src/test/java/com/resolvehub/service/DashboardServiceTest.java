package com.resolvehub.service;

import com.resolvehub.dto.response.DashboardSummaryResponse;
import com.resolvehub.dto.response.PriorityCountResponse;
import com.resolvehub.dto.response.StatusCountResponse;
import com.resolvehub.dto.response.WorkloadResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private DashboardService dashboardService;

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

    @Nested
    class SummaryTests {

        @Test
        void managerGetsGlobalSummary() {
            when(incidentRepository.count()).thenReturn(25L);
            when(incidentRepository.countByStatus(IncidentStatus.NEW)).thenReturn(5L);
            when(incidentRepository.countByStatus(IncidentStatus.ASSIGNED)).thenReturn(4L);
            when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(6L);
            when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(7L);
            when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(3L);
            when(incidentRepository.countByPriority(Priority.CRITICAL)).thenReturn(2L);
            when(incidentRepository.countByPriority(Priority.HIGH)).thenReturn(8L);

            DashboardSummaryResponse summary = dashboardService.getSummary(carolDetails);

            assertEquals(25L, summary.getTotalIncidents());
            assertEquals(5L, summary.getOpenIncidents());
            assertEquals(4L, summary.getAssignedIncidents());
            assertEquals(6L, summary.getInProgressIncidents());
            assertEquals(7L, summary.getResolvedIncidents());
            assertEquals(3L, summary.getClosedIncidents());
            assertEquals(2L, summary.getCriticalIncidents());
            assertEquals(8L, summary.getHighIncidents());
        }

        @Test
        void agentGetsAssignedSummary() {
            UUID agentId = agentBob.getId();
            when(incidentRepository.countByAssignedToId(agentId)).thenReturn(10L);
            when(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.NEW)).thenReturn(1L);
            when(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.ASSIGNED)).thenReturn(3L);
            when(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.IN_PROGRESS)).thenReturn(4L);
            when(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.RESOLVED)).thenReturn(2L);
            when(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.CLOSED)).thenReturn(0L);
            when(incidentRepository.countByAssignedToIdAndPriority(agentId, Priority.CRITICAL)).thenReturn(1L);
            when(incidentRepository.countByAssignedToIdAndPriority(agentId, Priority.HIGH)).thenReturn(3L);

            DashboardSummaryResponse summary = dashboardService.getSummary(bobDetails);

            assertEquals(10L, summary.getTotalIncidents());
            assertEquals(3L, summary.getAssignedIncidents());
            assertEquals(4L, summary.getInProgressIncidents());
        }

        @Test
        void userGetsOwnSummary() {
            UUID userId = userAlice.getId();
            when(incidentRepository.countByCreatedById(userId)).thenReturn(5L);
            when(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.NEW)).thenReturn(2L);
            when(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.ASSIGNED)).thenReturn(1L);
            when(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.IN_PROGRESS)).thenReturn(1L);
            when(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.RESOLVED)).thenReturn(1L);
            when(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.CLOSED)).thenReturn(0L);
            when(incidentRepository.countByCreatedByIdAndPriority(userId, Priority.CRITICAL)).thenReturn(0L);
            when(incidentRepository.countByCreatedByIdAndPriority(userId, Priority.HIGH)).thenReturn(1L);

            DashboardSummaryResponse summary = dashboardService.getSummary(aliceDetails);

            assertEquals(5L, summary.getTotalIncidents());
            assertEquals(2L, summary.getOpenIncidents());
        }
    }

    @Nested
    class IncidentsByStatusTests {

        @Test
        void managerGetsGlobalStatusCounts() {
            for (IncidentStatus status : IncidentStatus.values()) {
                when(incidentRepository.countByStatus(status)).thenReturn(3L);
            }

            List<StatusCountResponse> counts = dashboardService.getIncidentsByStatus(carolDetails);

            assertEquals(IncidentStatus.values().length, counts.size());
            assertEquals("NEW", counts.get(0).getStatus());
            assertEquals(3L, counts.get(0).getCount());
        }

        @Test
        void userGetsFilteredStatusCounts() {
            UUID userId = userAlice.getId();
            for (IncidentStatus status : IncidentStatus.values()) {
                when(incidentRepository.countByCreatedByIdAndStatus(userId, status)).thenReturn(1L);
            }

            List<StatusCountResponse> counts = dashboardService.getIncidentsByStatus(aliceDetails);

            assertEquals(IncidentStatus.values().length, counts.size());
            counts.forEach(c -> assertEquals(1L, c.getCount()));
        }
    }

    @Nested
    class IncidentsByPriorityTests {

        @Test
        void managerGetsGlobalPriorityCounts() {
            for (Priority priority : Priority.values()) {
                when(incidentRepository.countByPriority(priority)).thenReturn(5L);
            }

            List<PriorityCountResponse> counts = dashboardService.getIncidentsByPriority(carolDetails);

            assertEquals(Priority.values().length, counts.size());
            assertEquals("LOW", counts.get(0).getPriority());
            assertEquals(5L, counts.get(0).getCount());
        }
    }

    @Nested
    class WorkloadTests {

        @Test
        void managerWorkloadIncludesAgentBreakdown() {
            for (IncidentStatus status : IncidentStatus.values()) {
                when(incidentRepository.countByStatus(status)).thenReturn(2L);
            }

            UUID agentId = UUID.randomUUID();
            List<Object[]> agentRows = List.of(
                    new Object[]{agentId, "Bob", IncidentStatus.ASSIGNED, 3L},
                    new Object[]{agentId, "Bob", IncidentStatus.IN_PROGRESS, 2L}
            );
            when(incidentRepository.countByAgentAndStatus()).thenReturn(agentRows);

            WorkloadResponse workload = dashboardService.getWorkload(carolDetails);

            assertNotNull(workload.getAgentWorkloads());
            assertEquals(1, workload.getAgentWorkloads().size());
            assertEquals("Bob", workload.getAgentWorkloads().get(0).getAgentName());
            assertEquals(5L, workload.getAgentWorkloads().get(0).getTotalAssigned());
        }

        @Test
        void userWorkloadHasNoAgentBreakdown() {
            UUID userId = userAlice.getId();
            for (IncidentStatus status : IncidentStatus.values()) {
                when(incidentRepository.countByCreatedByIdAndStatus(userId, status)).thenReturn(1L);
            }

            WorkloadResponse workload = dashboardService.getWorkload(aliceDetails);

            assertNull(workload.getAgentWorkloads());
            assertEquals(IncidentStatus.values().length, workload.getMyTotal());
        }
    }
}
