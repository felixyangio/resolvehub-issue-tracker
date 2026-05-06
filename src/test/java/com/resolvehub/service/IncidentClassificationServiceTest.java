package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncidentClassificationServiceTest {

    private final IncidentClassificationService service = new IncidentClassificationService();

    @Test
    void securityCategoryReturnsHigh() {
        Priority result = service.classify("Password reset", "User forgot password", IncidentCategory.SECURITY);
        assertEquals(Priority.HIGH, result);
    }

    @ParameterizedTest
    @CsvSource({
            "System outage in prod, servers not responding",
            "Production down, entire cluster failed",
            "Data leak discovered, customer data exposed",
            "Ransomware detected, files encrypted"
    })
    void criticalKeywordsReturnCritical(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.TECHNICAL);
        assertEquals(Priority.CRITICAL, result);
    }

    @ParameterizedTest
    @CsvSource({
            "Payment failed for customer, checkout broken",
            "Login issue, users cannot authenticate",
            "Access denied on dashboard, unauthorized error",
            "Service unavailable, API returning 503"
    })
    void highKeywordsReturnHigh(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.TECHNICAL);
        assertEquals(Priority.HIGH, result);
    }

    @ParameterizedTest
    @CsvSource({
            "UI alignment issue, button off by 2px",
            "Typo in email template, wrong spelling",
            "Minor color mismatch, cosmetic issue",
            "Feature request for dark mode, suggestion from user"
    })
    void lowKeywordsReturnLow(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.TECHNICAL);
        assertEquals(Priority.LOW, result);
    }

    @Test
    void noMatchingKeywordsReturnMedium() {
        Priority result = service.classify("Server slow", "Response times are high", IncidentCategory.TECHNICAL);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void securityCategoryTakesPrecedenceOverCriticalKeywords() {
        Priority result = service.classify("System outage", "Production down", IncidentCategory.SECURITY);
        assertEquals(Priority.HIGH, result);
    }

    @Test
    void criticalTakesPrecedenceOverHigh() {
        Priority result = service.classify("Data leak causing service unavailable", "Both keywords", IncidentCategory.TECHNICAL);
        assertEquals(Priority.CRITICAL, result);
    }

    @Test
    void caseInsensitiveMatching() {
        Priority result = service.classify("SYSTEM DOWN", "PRODUCTION DOWN", IncidentCategory.TECHNICAL);
        assertEquals(Priority.CRITICAL, result);
    }
}
