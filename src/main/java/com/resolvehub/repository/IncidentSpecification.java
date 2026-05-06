package com.resolvehub.repository;

import com.resolvehub.entity.Incident;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class IncidentSpecification {

    private IncidentSpecification() {
    }

    public static Specification<Incident> hasStatus(IncidentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Incident> hasCategory(IncidentCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Incident> hasPriority(Priority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Incident> createdBy(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }

    public static Specification<Incident> assignedTo(UUID agentId) {
        return (root, query, cb) -> agentId == null ? null : cb.equal(root.get("assignedTo").get("id"), agentId);
    }
}
