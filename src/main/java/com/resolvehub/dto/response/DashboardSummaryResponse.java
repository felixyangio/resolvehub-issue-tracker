package com.resolvehub.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {

    private final long totalIncidents;
    private final long openIncidents;
    private final long assignedIncidents;
    private final long inProgressIncidents;
    private final long resolvedIncidents;
    private final long closedIncidents;
    private final long criticalIncidents;
    private final long highIncidents;
}
