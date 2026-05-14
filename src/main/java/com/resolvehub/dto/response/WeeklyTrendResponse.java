package com.resolvehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyTrendResponse {
    private final String day;
    private final long created;
    private final long resolved;
}
