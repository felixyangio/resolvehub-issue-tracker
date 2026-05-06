package com.resolvehub.controller;

import com.resolvehub.dto.response.DashboardSummaryResponse;
import com.resolvehub.dto.response.PriorityCountResponse;
import com.resolvehub.dto.response.StatusCountResponse;
import com.resolvehub.dto.response.WorkloadResponse;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getSummary(userDetails));
    }

    @GetMapping("/incidents-by-status")
    public ResponseEntity<List<StatusCountResponse>> getIncidentsByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getIncidentsByStatus(userDetails));
    }

    @GetMapping("/incidents-by-severity")
    public ResponseEntity<List<PriorityCountResponse>> getIncidentsByPriority(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getIncidentsByPriority(userDetails));
    }

    @GetMapping("/my-workload")
    public ResponseEntity<WorkloadResponse> getWorkload(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getWorkload(userDetails));
    }
}
