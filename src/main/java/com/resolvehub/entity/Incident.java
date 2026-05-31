package com.resolvehub.entity;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incident_status", columnList = "status"),
    @Index(name = "idx_incident_priority", columnList = "priority"),
    @Index(name = "idx_incident_category", columnList = "category"),
    @Index(name = "idx_incident_created_by", columnList = "created_by"),
    @Index(name = "idx_incident_assigned_to", columnList = "assigned_to"),
    @Index(name = "idx_incident_created_by_status", columnList = "created_by, status"),
    @Index(name = "idx_incident_assigned_to_status", columnList = "assigned_to, status"),
    @Index(name = "idx_incident_created_at", columnList = "created_at"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    private LocalDateTime dueAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
