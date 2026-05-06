package com.resolvehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.dto.request.AddCommentRequest;
import com.resolvehub.dto.response.CommentResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.security.JwtService;
import com.resolvehub.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.resolvehub.security.CustomUserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails aliceDetails;
    private UUID incidentId;

    @BeforeEach
    void setUp() {
        User alice = new User();
        alice.setId(UUID.randomUUID());
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        alice.setRole(Role.USER);
        alice.setEnabled(true);
        aliceDetails = new CustomUserDetails(alice);
        incidentId = UUID.randomUUID();
    }

    @Test
    void addCommentReturns201() throws Exception {
        CommentResponse response = CommentResponse.builder()
                .id(UUID.randomUUID())
                .content("This is urgent!")
                .authorId(aliceDetails.getId())
                .authorName("Alice")
                .authorRole("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentService.addComment(eq(incidentId), any(AddCommentRequest.class), any(CustomUserDetails.class)))
                .thenReturn(response);

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("This is urgent!");

        mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                        .with(user(aliceDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("This is urgent!"))
                .andExpect(jsonPath("$.authorName").value("Alice"));
    }

    @Test
    void addBlankCommentReturns400() throws Exception {
        AddCommentRequest request = new AddCommentRequest();
        request.setContent("");

        mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                        .with(user(aliceDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Content is required"));
    }

    @Test
    void getCommentsReturns200() throws Exception {
        CommentResponse comment = CommentResponse.builder()
                .id(UUID.randomUUID())
                .content("A comment")
                .authorId(aliceDetails.getId())
                .authorName("Alice")
                .authorRole("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentService.getComments(eq(incidentId), any(CustomUserDetails.class)))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/api/incidents/{incidentId}/comments", incidentId)
                        .with(user(aliceDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("A comment"));
    }

    @Test
    void addCommentReturns403WhenAccessDenied() throws Exception {
        when(commentService.addComment(eq(incidentId), any(AddCommentRequest.class), any(CustomUserDetails.class)))
                .thenThrow(new AccessDeniedException("You do not have permission to view this incident"));

        AddCommentRequest request = new AddCommentRequest();
        request.setContent("Should not work");

        mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                        .with(user(aliceDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
