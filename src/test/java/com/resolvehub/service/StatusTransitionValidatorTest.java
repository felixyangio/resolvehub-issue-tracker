package com.resolvehub.service;

import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTransitionValidatorTest {

    private StatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StatusTransitionValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "NEW, ASSIGNED",
            "NEW, CANCELLED",
            "ASSIGNED, IN_PROGRESS",
            "ASSIGNED, CANCELLED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CANCELLED",
            "RESOLVED, CLOSED"
    })
    void shouldAllowValidTransitions(IncidentStatus from, IncidentStatus to) {
        assertDoesNotThrow(() -> validator.validate(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "NEW, IN_PROGRESS",
            "NEW, RESOLVED",
            "NEW, CLOSED",
            "ASSIGNED, RESOLVED",
            "ASSIGNED, CLOSED",
            "IN_PROGRESS, ASSIGNED",
            "IN_PROGRESS, NEW",
            "RESOLVED, IN_PROGRESS",
            "RESOLVED, NEW",
            "CLOSED, NEW",
            "CLOSED, ASSIGNED",
            "CANCELLED, NEW",
            "CANCELLED, ASSIGNED"
    })
    void shouldRejectInvalidTransitions(IncidentStatus from, IncidentStatus to) {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validate(from, to));
        assertTrue(ex.getMessage().contains("Cannot transition from " + from + " to " + to));
    }

    @Test
    void shouldRejectTransitionToSameStatus() {
        assertThrows(BadRequestException.class,
                () -> validator.validate(IncidentStatus.NEW, IncidentStatus.NEW));
    }
}
