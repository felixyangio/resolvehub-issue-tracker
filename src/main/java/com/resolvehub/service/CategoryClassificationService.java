package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CategoryClassificationService {

    private static final List<String> SAFETY_KEYWORDS = List.of(
            "gas leak", "gas smell", "smell of gas", "fire", "smoke alarm",
            "carbon monoxide", "electrocution", "electrical shock", "collapse",
            "asbestos", "hazard", "hazardous", "dangerous", "unsafe",
            "emergency evacuation", "fire alarm", "fire safety", "safety hazard",
            "safety concern", "fire extinguisher", "fire exit", "evacuation"
    );

    private static final Map<IncidentCategory, List<String>> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_MAP.put(IncidentCategory.MAINTENANCE, List.of(
                "leak", "burst pipe", "pipe", "plumbing", "boiler", "radiator",
                "heating", "hot water", "tap", "drain", "blocked",
                "broken", "repair", "fix", "damage", "crack", "damp", "mould", "mold",
                "window", "door", "roof", "ceiling", "floor", "wall",
                "appliance", "washing machine", "dishwasher", "oven", "fridge",
                "toilet", "shower", "bath", "sink", "sewage"
        ));

        KEYWORD_MAP.put(IncidentCategory.NOISE, List.of(
                "noise", "loud", "music", "party", "shouting", "barking",
                "construction noise", "drilling", "banging", "disturbance",
                "antisocial", "anti-social", "quiet hours"
        ));

        KEYWORD_MAP.put(IncidentCategory.INTERNET, List.of(
                "internet", "wifi", "wi-fi", "broadband", "network",
                "router", "connection", "signal", "ethernet", "slow speed",
                "no connection", "outage", "disconnected"
        ));

        KEYWORD_MAP.put(IncidentCategory.BILLING, List.of(
                "bill", "invoice", "charge", "payment", "overcharge",
                "electricity bill", "water bill", "gas bill", "rent",
                "direct debit", "refund", "account balance", "arrears"
        ));

        KEYWORD_MAP.put(IncidentCategory.DEPOSIT, List.of(
                "deposit", "deduction", "check-out", "checkout inspection",
                "inventory", "end of tenancy", "bond", "deposit return",
                "deposit dispute", "damage charge"
        ));

        KEYWORD_MAP.put(IncidentCategory.CLEANING, List.of(
                "cleaning", "clean", "dirty", "rubbish", "litter", "bin",
                "pest", "infestation", "mice", "rats", "cockroach", "ants",
                "communal area", "hallway dirty", "hygiene", "mop", "vacuum"
        ));

        KEYWORD_MAP.put(IncidentCategory.ACCESS, List.of(
                "key", "lock", "fob", "access card", "keycard",
                "locked out", "entry", "gate", "intercom", "buzzer",
                "security door", "access code", "swipe card", "lost key"
        ));
    }

    public IncidentCategory classify(String title, String description) {
        String text = (title + " " + description).toLowerCase(Locale.ENGLISH);

        if (containsAny(text, SAFETY_KEYWORDS)) {
            return IncidentCategory.SAFETY;
        }

        IncidentCategory best = null;
        int bestCount = 0;

        for (Map.Entry<IncidentCategory, List<String>> entry : KEYWORD_MAP.entrySet()) {
            int count = 0;
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) {
                    count++;
                }
            }
            if (count > bestCount) {
                bestCount = count;
                best = entry.getKey();
            }
        }

        return best != null ? best : IncidentCategory.OTHER;
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}
