package com.resolvehub.dto.request;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.Priority;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateIncidentRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private IncidentCategory category;

    private Priority priority;

    private LocalDateTime dueAt;
}
