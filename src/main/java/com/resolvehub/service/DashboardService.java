package com.resolvehub.service;

import com.resolvehub.dto.response.AgentWorkloadResponse;
import com.resolvehub.dto.response.CategoryCountResponse;
import com.resolvehub.dto.response.DashboardSummaryResponse;
import com.resolvehub.dto.response.IncidentResponse;
import com.resolvehub.dto.response.PriorityCountResponse;
import com.resolvehub.dto.response.StatusCountResponse;
import com.resolvehub.dto.response.WeeklyTrendResponse;
import com.resolvehub.dto.response.WorkloadResponse;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final IncidentRepository incidentRepository;

    public DashboardSummaryResponse getSummary(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        if (role == Role.MANAGER || role == Role.ADMIN) {
            return buildGlobalSummary();
        } else if (role == Role.AGENT) {
            return buildAgentSummary(userDetails.getId());
        } else {
            return buildUserSummary(userDetails.getId());
        }
    }

    public List<StatusCountResponse> getIncidentsByStatus(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        return Arrays.stream(IncidentStatus.values())
                .map(status -> new StatusCountResponse(
                        status.name(),
                        countByStatus(role, userDetails.getId(), status)))
                .toList();
    }

    public List<PriorityCountResponse> getIncidentsByPriority(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        return Arrays.stream(Priority.values())
                .map(priority -> new PriorityCountResponse(
                        priority.name(),
                        countByPriority(role, userDetails.getId(), priority)))
                .toList();
    }

    public List<CategoryCountResponse> getIncidentsByCategory(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        return Arrays.stream(IncidentCategory.values())
                .map(cat -> {
                    long count;
                    if (role == Role.MANAGER || role == Role.ADMIN) {
                        count = incidentRepository.countByCategory(cat);
                    } else if (role == Role.AGENT) {
                        count = incidentRepository.countByCategoryAndAssignedToId(cat, userDetails.getId());
                    } else {
                        count = incidentRepository.countByCategoryAndCreatedById(cat, userDetails.getId());
                    }
                    return new CategoryCountResponse(cat.name(), count);
                })
                .toList();
    }

    public List<IncidentResponse> getRecentActivity(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());

        if (role == Role.MANAGER || role == Role.ADMIN) {
            return incidentRepository.findTop10ByOrderByUpdatedAtDesc().stream()
                    .map(IncidentResponse::fromEntity)
                    .toList();
        } else if (role == Role.AGENT) {
            return incidentRepository.findTop10ByAssignedToIdOrderByUpdatedAtDesc(userDetails.getId()).stream()
                    .map(IncidentResponse::fromEntity)
                    .toList();
        } else {
            return incidentRepository.findTop10ByCreatedByIdOrderByUpdatedAtDesc(userDetails.getId()).stream()
                    .map(IncidentResponse::fromEntity)
                    .toList();
        }
    }

    public List<WeeklyTrendResponse> getWeeklyTrend() {
        List<WeeklyTrendResponse> trend = new ArrayList<>();
        LocalDateTime now = LocalDate.now().atStartOfDay();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE");

        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            String dayName = dayStart.format(dayFormat);
            long created = incidentRepository.countCreatedBetween(dayStart, dayEnd);
            long resolved = incidentRepository.countResolvedBetween(dayStart, dayEnd);
            trend.add(new WeeklyTrendResponse(dayName, created, resolved));
        }
        return trend;
    }

    public WorkloadResponse getWorkload(CustomUserDetails userDetails) {
        Role role = Role.valueOf(userDetails.getRole());
        UUID userId = userDetails.getId();

        List<StatusCountResponse> myStatusCounts = Arrays.stream(IncidentStatus.values())
                .map(status -> new StatusCountResponse(
                        status.name(),
                        countByStatus(role, userId, status)))
                .toList();

        long myTotal = myStatusCounts.stream().mapToLong(StatusCountResponse::getCount).sum();

        List<AgentWorkloadResponse> agentWorkloads = null;
        if (role == Role.MANAGER || role == Role.ADMIN) {
            agentWorkloads = buildAgentWorkloads();
        }

        return WorkloadResponse.builder()
                .myStatusCounts(myStatusCounts)
                .myTotal(myTotal)
                .agentWorkloads(agentWorkloads)
                .build();
    }

    // --- private helpers ---

    private DashboardSummaryResponse buildGlobalSummary() {
        return DashboardSummaryResponse.builder()
                .totalIncidents(incidentRepository.count())
                .openIncidents(incidentRepository.countByStatus(IncidentStatus.NEW))
                .assignedIncidents(incidentRepository.countByStatus(IncidentStatus.ASSIGNED))
                .inProgressIncidents(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS))
                .resolvedIncidents(incidentRepository.countByStatus(IncidentStatus.RESOLVED))
                .closedIncidents(incidentRepository.countByStatus(IncidentStatus.CLOSED))
                .criticalIncidents(incidentRepository.countByPriority(Priority.CRITICAL))
                .highIncidents(incidentRepository.countByPriority(Priority.HIGH))
                .build();
    }

    private DashboardSummaryResponse buildAgentSummary(UUID agentId) {
        return DashboardSummaryResponse.builder()
                .totalIncidents(incidentRepository.countByAssignedToId(agentId))
                .openIncidents(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.NEW))
                .assignedIncidents(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.ASSIGNED))
                .inProgressIncidents(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.IN_PROGRESS))
                .resolvedIncidents(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.RESOLVED))
                .closedIncidents(incidentRepository.countByAssignedToIdAndStatus(agentId, IncidentStatus.CLOSED))
                .criticalIncidents(incidentRepository.countByAssignedToIdAndPriority(agentId, Priority.CRITICAL))
                .highIncidents(incidentRepository.countByAssignedToIdAndPriority(agentId, Priority.HIGH))
                .build();
    }

    private DashboardSummaryResponse buildUserSummary(UUID userId) {
        return DashboardSummaryResponse.builder()
                .totalIncidents(incidentRepository.countByCreatedById(userId))
                .openIncidents(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.NEW))
                .assignedIncidents(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.ASSIGNED))
                .inProgressIncidents(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.IN_PROGRESS))
                .resolvedIncidents(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.RESOLVED))
                .closedIncidents(incidentRepository.countByCreatedByIdAndStatus(userId, IncidentStatus.CLOSED))
                .criticalIncidents(incidentRepository.countByCreatedByIdAndPriority(userId, Priority.CRITICAL))
                .highIncidents(incidentRepository.countByCreatedByIdAndPriority(userId, Priority.HIGH))
                .build();
    }

    private long countByStatus(Role role, UUID userId, IncidentStatus status) {
        if (role == Role.MANAGER || role == Role.ADMIN) {
            return incidentRepository.countByStatus(status);
        } else if (role == Role.AGENT) {
            return incidentRepository.countByAssignedToIdAndStatus(userId, status);
        } else {
            return incidentRepository.countByCreatedByIdAndStatus(userId, status);
        }
    }

    private long countByPriority(Role role, UUID userId, Priority priority) {
        if (role == Role.MANAGER || role == Role.ADMIN) {
            return incidentRepository.countByPriority(priority);
        } else if (role == Role.AGENT) {
            return incidentRepository.countByAssignedToIdAndPriority(userId, priority);
        } else {
            return incidentRepository.countByCreatedByIdAndPriority(userId, priority);
        }
    }

    private List<AgentWorkloadResponse> buildAgentWorkloads() {
        List<Object[]> rows = incidentRepository.countByAgentAndStatus();

        Map<UUID, AgentWorkloadResponse.AgentWorkloadBuilder> builders = new LinkedHashMap<>();

        for (Object[] row : rows) {
            UUID agentId = (UUID) row[0];
            String agentName = (String) row[1];
            IncidentStatus status = (IncidentStatus) row[2];
            long count = (Long) row[3];

            builders.computeIfAbsent(agentId, id -> new AgentWorkloadResponse.AgentWorkloadBuilder(id, agentName));
            builders.get(agentId).add(status.name(), count);
        }

        return builders.values().stream()
                .map(AgentWorkloadResponse.AgentWorkloadBuilder::build)
                .toList();
    }
}
