package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.Priority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class IncidentClassificationService {

    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "flood", "gas leak", "gas smell", "fire", "no heating",
            "burst pipe", "sewage", "collapse", "electrocution"
    );

    private static final List<String> HIGH_KEYWORDS = List.of(
            "leak", "broken lock", "boiler", "no hot water", "mould",
            "unsafe", "electrical fault", "no power", "security",
            "infestation", "not working"
    );

    private static final List<String> LOW_KEYWORDS = List.of(
            "lightbulb", "minor", "question", "query", "information",
            "bin", "cosmetic", "suggestion", "paint"
    );

    public Priority classify(String title, String description, IncidentCategory category) {
        String text = (title + " " + description).toLowerCase(Locale.ENGLISH);

        if (category == IncidentCategory.SAFETY) {
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
