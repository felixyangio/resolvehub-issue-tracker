package com.resolvehub.repository;

import com.resolvehub.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
