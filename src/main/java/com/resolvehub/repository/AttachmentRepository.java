package com.resolvehub.repository;

import com.resolvehub.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);

    long countByIncidentId(UUID incidentId);
}
