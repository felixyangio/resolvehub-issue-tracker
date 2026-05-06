package com.resolvehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatusCountResponse {

    private final String status;
    private final long count;
}
