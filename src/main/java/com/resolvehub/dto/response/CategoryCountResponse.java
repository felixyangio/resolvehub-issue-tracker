package com.resolvehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryCountResponse {

    private final String category;
    private final long count;
}
