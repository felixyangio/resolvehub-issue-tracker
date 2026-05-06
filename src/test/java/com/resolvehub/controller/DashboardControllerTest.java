package com.resolvehub.controller;

import com.resolvehub.dto.response.DashboardSummaryResponse;
import com.resolvehub.entity.User;
import com.resolvehub.enums.Role;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.security.JwtService;
import com.resolvehub.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.resolvehub.security.CustomUserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails carolDetails;

    @BeforeEach
    void setUp() {
        User carol = new User();
        carol.setId(UUID.randomUUID());
        carol.setName("Carol");
        carol.setEmail("carol@example.com");
        carol.setRole(Role.MANAGER);
        carol.setEnabled(true);
        carolDetails = new CustomUserDetails(carol);
    }

    @Test
    void summaryReturns200WithCounts() throws Exception {
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalIncidents(25)
                .openIncidents(5)
                .assignedIncidents(4)
                .inProgressIncidents(6)
                .resolvedIncidents(7)
                .closedIncidents(3)
                .criticalIncidents(2)
                .highIncidents(8)
                .build();

        when(dashboardService.getSummary(any(CustomUserDetails.class))).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user(carolDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").value(25))
                .andExpect(jsonPath("$.openIncidents").value(5))
                .andExpect(jsonPath("$.criticalIncidents").value(2));
    }

    @Test
    void summaryReturns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void incidentsByStatusReturns200() throws Exception {
        when(dashboardService.getIncidentsByStatus(any(CustomUserDetails.class)))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/dashboard/incidents-by-status")
                        .with(user(carolDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void incidentsBySeverityReturns200() throws Exception {
        when(dashboardService.getIncidentsByPriority(any(CustomUserDetails.class)))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/dashboard/incidents-by-severity")
                        .with(user(carolDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void myWorkloadReturns200() throws Exception {
        when(dashboardService.getWorkload(any(CustomUserDetails.class)))
                .thenReturn(com.resolvehub.dto.response.WorkloadResponse.builder()
                        .myStatusCounts(java.util.List.of())
                        .myTotal(0)
                        .build());

        mockMvc.perform(get("/api/dashboard/my-workload")
                        .with(user(carolDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myTotal").value(0));
    }
}
