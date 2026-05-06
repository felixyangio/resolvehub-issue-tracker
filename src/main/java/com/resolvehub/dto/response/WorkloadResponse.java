package com.resolvehub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkloadResponse {

    private final List<StatusCountResponse> myStatusCounts;
    private final long myTotal;
    private final List<AgentWorkloadResponse> agentWorkloads;
}
