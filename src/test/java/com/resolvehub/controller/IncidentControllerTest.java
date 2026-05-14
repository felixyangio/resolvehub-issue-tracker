package com.resolvehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.dto.request.AssignIncidentRequest;
import com.resolvehub.dto.request.CreateIncidentRequest;
import com.resolvehub.dto.request.UpdateStatusRequest;
import com.resolvehub.dto.response.IncidentResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.security.JwtService;
import com.resolvehub.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.resolvehub.security.CustomUserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails aliceDetails;
    private CustomUserDetails carolDetails;

    @BeforeEach
    void setUp() {
        User alice = new User();
        alice.setId(UUID.randomUUID());
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        alice.setRole(Role.USER);
        alice.setEnabled(true);
        aliceDetails = new CustomUserDetails(alice);

        User carol = new User();
        carol.setId(UUID.randomUUID());
        carol.setName("Carol");
        carol.setEmail("carol@example.com");
        carol.setRole(Role.MANAGER);
        carol.setEnabled(true);
        carolDetails = new CustomUserDetails(carol);
    }

    private IncidentResponse sampleResponse() {
        return IncidentResponse.builder()
                .id(UUID.randomUUID())
                .title("Server down")
                .description("API not responding")
                .category("MAINTENANCE")
                .priority("HIGH")
                .status("NEW")
                .createdById(aliceDetails.getId())
                .createdByName("Alice")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class CreateIncidentTests {

        @Test
        void returns201WhenValid() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class), any(CustomUserDetails.class)))
                    .thenReturn(sampleResponse());

            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setTitle("Server down");
            request.setDescription("API not responding");
            request.setCategory(IncidentCategory.MAINTENANCE);

            mockMvc.perform(post("/api/incidents")
                            .with(user(aliceDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Server down"))
                    .andExpect(jsonPath("$.status").value("NEW"));
        }

        @Test
        void returns400WhenTitleMissing() throws Exception {
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setDescription("Some description");
            request.setCategory(IncidentCategory.MAINTENANCE);

            mockMvc.perform(post("/api/incidents")
                            .with(user(aliceDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").value("Title is required"));
        }

        @Test
        void returns201WhenCategoryOmittedAndAutoDetected() throws Exception {
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setTitle("Boiler broken");
            request.setDescription("No hot water since yesterday");

            mockMvc.perform(post("/api/incidents")
                            .with(user(aliceDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void returns401WhenUnauthenticated() throws Exception {
            CreateIncidentRequest request = new CreateIncidentRequest();
            request.setTitle("Test");
            request.setDescription("Desc");
            request.setCategory(IncidentCategory.MAINTENANCE);

            mockMvc.perform(post("/api/incidents")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetIncidentTests {

        @Test
        void returns200WhenFound() throws Exception {
            IncidentResponse response = sampleResponse();
            when(incidentService.findById(any(UUID.class), any(CustomUserDetails.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/api/incidents/{id}", response.getId())
                            .with(user(aliceDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Server down"));
        }

        @Test
        void returns403WhenAccessDenied() throws Exception {
            UUID incidentId = UUID.randomUUID();
            when(incidentService.findById(eq(incidentId), any(CustomUserDetails.class)))
                    .thenThrow(new AccessDeniedException("You do not have permission to view this incident"));

            mockMvc.perform(get("/api/incidents/{id}", incidentId)
                            .with(user(aliceDetails)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You do not have permission to view this incident"));
        }
    }

    @Nested
    class AssignIncidentTests {

        @Test
        void managerCanAssign() throws Exception {
            UUID incidentId = UUID.randomUUID();
            UUID agentId = UUID.randomUUID();
            IncidentResponse response = IncidentResponse.builder()
                    .id(incidentId)
                    .title("Server down")
                    .description("Desc")
                    .category("MAINTENANCE")
                    .priority("HIGH")
                    .status("ASSIGNED")
                    .createdById(UUID.randomUUID())
                    .createdByName("Alice")
                    .assignedToId(agentId)
                    .assignedToName("Bob")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(incidentService.assign(eq(incidentId), any(AssignIncidentRequest.class), any(CustomUserDetails.class)))
                    .thenReturn(response);

            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setAgentId(agentId);

            mockMvc.perform(patch("/api/incidents/{id}/assign", incidentId)
                            .with(user(carolDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ASSIGNED"))
                    .andExpect(jsonPath("$.assignedToName").value("Bob"));
        }

        @Test
        void returns400WhenAgentIdMissing() throws Exception {
            AssignIncidentRequest request = new AssignIncidentRequest();

            mockMvc.perform(patch("/api/incidents/{id}/assign", UUID.randomUUID())
                            .with(user(carolDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.agentId").value("Agent ID is required"));
        }
    }

    @Nested
    class UpdateStatusTests {

        @Test
        void returns200OnValidTransition() throws Exception {
            UUID incidentId = UUID.randomUUID();
            IncidentResponse response = IncidentResponse.builder()
                    .id(incidentId)
                    .title("Test")
                    .description("Desc")
                    .category("MAINTENANCE")
                    .priority("MEDIUM")
                    .status("IN_PROGRESS")
                    .createdById(UUID.randomUUID())
                    .createdByName("Alice")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(incidentService.updateStatus(eq(incidentId), any(UpdateStatusRequest.class), any(CustomUserDetails.class)))
                    .thenReturn(response);

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(IncidentStatus.IN_PROGRESS);

            mockMvc.perform(patch("/api/incidents/{id}/status", incidentId)
                            .with(user(carolDetails))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }
    }
}
