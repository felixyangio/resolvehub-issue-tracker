package com.resolvehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * File attachment linked to an incident.
 *
 * <p>Files are stored on the local filesystem (configurable path) with
 * a UUID-based filename to prevent collisions. The original filename,
 * MIME type, and size are preserved for display and validation.</p>
 */
@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /** Original filename as uploaded by the user */
    @Column(nullable = false, length = 255)
    private String originalName;

    /** UUID-based filename on disk */
    @Column(nullable = false, length = 255)
    private String storedName;

    /** MIME type, e.g. "image/png", "application/pdf" */
    @Column(nullable = false, length = 100)
    private String contentType;

    /** File size in bytes */
    @Column(nullable = false)
    private long fileSize;
}
