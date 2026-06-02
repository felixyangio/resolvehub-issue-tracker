package com.resolvehub.repository;

import com.resolvehub.entity.Incident;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {

    @Override
    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    Page<Incident> findAll(Specification<Incident> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    Optional<Incident> findById(UUID id);

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

    // Category counts
    long countByCategory(IncidentCategory category);

    // Category counts by user/agent
    long countByCategoryAndCreatedById(IncidentCategory category, UUID userId);

    long countByCategoryAndAssignedToId(IncidentCategory category, UUID agentId);

    // Recent activity — latest incidents
    List<Incident> findTop10ByOrderByUpdatedAtDesc();

    List<Incident> findTop10ByCreatedByIdOrderByUpdatedAtDesc(UUID userId);

    List<Incident> findTop10ByAssignedToIdOrderByUpdatedAtDesc(UUID agentId);

    // Weekly trend
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.createdAt >= :start AND i.createdAt < :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status IN (com.resolvehub.enums.IncidentStatus.RESOLVED, com.resolvehub.enums.IncidentStatus.CLOSED) AND i.updatedAt >= :start AND i.updatedAt < :end")
    long countResolvedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // SLA breach detection — active cases past their due date
    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    @Query("SELECT i FROM Incident i WHERE i.dueAt IS NOT NULL AND i.dueAt < :now " +
           "AND i.status IN (com.resolvehub.enums.IncidentStatus.NEW, " +
           "com.resolvehub.enums.IncidentStatus.ASSIGNED, " +
           "com.resolvehub.enums.IncidentStatus.IN_PROGRESS)")
    List<Incident> findOverdueIncidents(@Param("now") LocalDateTime now);
}
