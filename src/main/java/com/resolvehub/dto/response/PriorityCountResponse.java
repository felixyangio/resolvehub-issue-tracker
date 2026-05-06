package com.resolvehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PriorityCountResponse {

    private final String priority;
    private final long count;
}
