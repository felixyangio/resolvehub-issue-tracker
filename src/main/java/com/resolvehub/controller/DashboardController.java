package com.resolvehub.controller;

import com.resolvehub.dto.response.CategoryCountResponse;
import com.resolvehub.dto.response.DashboardSummaryResponse;
import com.resolvehub.dto.response.IncidentResponse;
import com.resolvehub.dto.response.PriorityCountResponse;
import com.resolvehub.dto.response.StatusCountResponse;
import com.resolvehub.dto.response.WeeklyTrendResponse;
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

    @GetMapping("/incidents-by-category")
    public ResponseEntity<List<CategoryCountResponse>> getIncidentsByCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getIncidentsByCategory(userDetails));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<IncidentResponse>> getRecentActivity(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(userDetails));
    }

    @GetMapping("/weekly-trend")
    public ResponseEntity<List<WeeklyTrendResponse>> getWeeklyTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getWeeklyTrend());
    }

    @GetMapping("/my-workload")
    public ResponseEntity<WorkloadResponse> getWorkload(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(dashboardService.getWorkload(userDetails));
    }
}
