package com.resolvehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AgentWorkloadResponse {

    private final UUID agentId;
    private final String agentName;
    private final List<StatusCountResponse> statusCounts;
    private final long totalAssigned;

    public static class AgentWorkloadBuilder {

        private final UUID agentId;
        private final String agentName;
        private final List<StatusCountResponse> statusCounts = new ArrayList<>();
        private long total;

        public AgentWorkloadBuilder(UUID agentId, String agentName) {
            this.agentId = agentId;
            this.agentName = agentName;
        }

        public void add(String status, long count) {
            statusCounts.add(new StatusCountResponse(status, count));
            total += count;
        }

        public AgentWorkloadResponse build() {
            return new AgentWorkloadResponse(agentId, agentName, statusCounts, total);
        }
    }
}
