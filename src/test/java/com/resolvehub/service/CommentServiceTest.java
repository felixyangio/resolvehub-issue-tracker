package com.resolvehub.service;

import com.resolvehub.dto.request.AddCommentRequest;
import com.resolvehub.dto.response.CommentResponse;
import com.resolvehub.entity.Comment;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.repository.CommentRepository;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncidentService incidentService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CommentService commentService;

    private User userAlice;
    private User agentBob;
    private CustomUserDetails aliceDetails;
    private CustomUserDetails bobDetails;
    private Incident incident;

    @BeforeEach
    void setUp() {
        userAlice = new User();
        userAlice.setId(UUID.randomUUID());
        userAlice.setName("Alice");
        userAlice.setEmail("alice@example.com");
        userAlice.setRole(Role.USER);
        userAlice.setEnabled(true);

        agentBob = new User();
        agentBob.setId(UUID.randomUUID());
        agentBob.setName("Bob");
        agentBob.setEmail("bob@example.com");
        agentBob.setRole(Role.AGENT);
        agentBob.setEnabled(true);

        aliceDetails = new CustomUserDetails(userAlice);
        bobDetails = new CustomUserDetails(agentBob);

        incident = new Incident();
        incident.setId(UUID.randomUUID());
        incident.setTitle("Test incident");
        incident.setDescription("Test");
        incident.setCategory(IncidentCategory.TECHNICAL);
        incident.setPriority(Priority.MEDIUM);
        incident.setStatus(IncidentStatus.NEW);
        incident.setCreatedBy(userAlice);
    }

    @Test
    void ownerCanAddComment() {
        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, aliceDetails);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("This is urgent!");

        CommentResponse response = commentService.addComment(incident.getId(), request, aliceDetails);

        assertNotNull(response.getId());
        assertEquals("This is urgent!", response.getContent());
        assertEquals("Alice", response.getAuthorName());
        assertEquals("USER", response.getAuthorRole());
    }

    @Test
    void unauthorizedUserCannotAddComment() {
        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doThrow(new AccessDeniedException("You do not have permission to view this incident"))
                .when(incidentService).checkReadAccess(incident, bobDetails);

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("Should not work");

        assertThrows(AccessDeniedException.class,
                () -> commentService.addComment(incident.getId(), request, bobDetails));
    }

    @Test
    void ownerCanViewComments() {
        Comment comment = Comment.builder()
                .content("A comment")
                .incident(incident)
                .author(userAlice)
                .build();
        comment.setId(UUID.randomUUID());

        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, aliceDetails);
        when(commentRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId()))
                .thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getComments(incident.getId(), aliceDetails);

        assertEquals(1, responses.size());
        assertEquals("A comment", responses.get(0).getContent());
    }

    @Test
    void unauthorizedUserCannotViewComments() {
        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doThrow(new AccessDeniedException("You do not have permission to view this incident"))
                .when(incidentService).checkReadAccess(incident, bobDetails);

        assertThrows(AccessDeniedException.class,
                () -> commentService.getComments(incident.getId(), bobDetails));
    }

    @Test
    void agentCanCommentOnAssignedIncident() {
        incident.setAssignedTo(agentBob);
        incident.setStatus(IncidentStatus.ASSIGNED);

        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, bobDetails);
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(agentBob));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("Working on it");

        CommentResponse response = commentService.addComment(incident.getId(), request, bobDetails);

        assertEquals("Working on it", response.getContent());
        assertEquals("Bob", response.getAuthorName());
        assertEquals("AGENT", response.getAuthorRole());
    }

    @Test
    void managerCanCommentOnAnyIncident() {
        User managerCarol = new User();
        managerCarol.setId(UUID.randomUUID());
        managerCarol.setName("Carol");
        managerCarol.setEmail("carol@example.com");
        managerCarol.setRole(Role.MANAGER);
        managerCarol.setEnabled(true);
        CustomUserDetails carolDetails = new CustomUserDetails(managerCarol);

        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, carolDetails);
        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(managerCarol));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("Please prioritise this");

        CommentResponse response = commentService.addComment(incident.getId(), request, carolDetails);

        assertEquals("Please prioritise this", response.getContent());
        assertEquals("MANAGER", response.getAuthorRole());
    }

    @Test
    void addCommentGeneratesAuditLog() {
        when(incidentService.getIncidentOrThrow(incident.getId())).thenReturn(incident);
        doNothing().when(incidentService).checkReadAccess(incident, aliceDetails);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("Test comment");

        commentService.addComment(incident.getId(), request, aliceDetails);

        verify(auditLogService).log(
                eq(incident), eq(userAlice),
                eq(com.resolvehub.enums.AuditAction.COMMENT_ADDED),
                eq(null), eq(null),
                eq("Comment added by Alice"));
    }
}
