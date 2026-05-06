package com.resolvehub.service;

import com.resolvehub.dto.request.AddCommentRequest;
import com.resolvehub.dto.response.CommentResponse;
import com.resolvehub.entity.Comment;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.AuditAction;
import com.resolvehub.exception.ResourceNotFoundException;
import com.resolvehub.repository.CommentRepository;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final IncidentService incidentService;
    private final AuditLogService auditLogService;

    @Transactional
    public CommentResponse addComment(UUID incidentId, AddCommentRequest request, CustomUserDetails userDetails) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);

        User author = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .incident(incident)
                .author(author)
                .build();

        Comment saved = commentRepository.save(comment);

        auditLogService.log(incident, author, AuditAction.COMMENT_ADDED,
                null, null, "Comment added by " + author.getName());

        return CommentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID incidentId, CustomUserDetails userDetails) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);

        return commentRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }
}
