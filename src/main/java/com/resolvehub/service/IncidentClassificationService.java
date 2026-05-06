package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.Priority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class IncidentClassificationService {

    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "outage", "system down", "data leak", "breach", "ransomware", "production down"
    );

    private static final List<String> HIGH_KEYWORDS = List.of(
            "security", "payment failed", "login", "authentication", "access denied",
            "unauthorized", "data loss", "service unavailable", "downtime"
    );

    private static final List<String> LOW_KEYWORDS = List.of(
            "ui", "typo", "minor", "cosmetic", "question", "feature request",
            "documentation", "suggestion"
    );

    public Priority classify(String title, String description, IncidentCategory category) {
        String text = (title + " " + description).toLowerCase(Locale.ENGLISH);

        if (category == IncidentCategory.SECURITY) {
            return Priority.HIGH;
        }

        if (containsAny(text, CRITICAL_KEYWORDS)) {
            return Priority.CRITICAL;
        }

        if (containsAny(text, HIGH_KEYWORDS)) {
            return Priority.HIGH;
        }

        if (containsAny(text, LOW_KEYWORDS)) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}
