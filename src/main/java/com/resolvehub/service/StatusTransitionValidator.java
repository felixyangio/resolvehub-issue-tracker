package com.resolvehub.service;

import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.resolvehub.enums.IncidentStatus.*;

@Component
public class StatusTransitionValidator {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = Map.of(
            NEW, Set.of(ASSIGNED, CANCELLED),
            ASSIGNED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(RESOLVED, CANCELLED),
            RESOLVED, Set.of(CLOSED),
            CLOSED, Set.of(),
            CANCELLED, Set.of()
    );

    public void validate(IncidentStatus current, IncidentStatus target) {
        Set<IncidentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(target)) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                            current, target, allowed)
            );
        }
    }
}
