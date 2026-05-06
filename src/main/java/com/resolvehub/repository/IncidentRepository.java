package com.resolvehub.repository;

import com.resolvehub.entity.Incident;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {

    Page<Incident> findByCreatedById(UUID userId, Pageable pageable);

    Page<Incident> findByAssignedToId(UUID agentId, Pageable pageable);

    // Global counts
    long countByStatus(IncidentStatus status);

    long countByPriority(Priority priority);

    // User-scoped counts
    long countByCreatedByIdAndStatus(UUID userId, IncidentStatus status);

    long countByCreatedByIdAndPriority(UUID userId, Priority priority);

    long countByCreatedById(UUID userId);

    // Agent-scoped counts
    long countByAssignedToIdAndStatus(UUID agentId, IncidentStatus status);

    long countByAssignedToIdAndPriority(UUID agentId, Priority priority);

    long countByAssignedToId(UUID agentId);

    // Agent workload for managers
    @Query("SELECT i.assignedTo.id, i.assignedTo.name, i.status, COUNT(i) " +
           "FROM Incident i WHERE i.assignedTo IS NOT NULL " +
           "GROUP BY i.assignedTo.id, i.assignedTo.name, i.status")
    List<Object[]> countByAgentAndStatus();
}
