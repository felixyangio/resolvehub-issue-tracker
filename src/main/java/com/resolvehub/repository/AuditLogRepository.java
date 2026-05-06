package com.resolvehub.repository;

import com.resolvehub.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
