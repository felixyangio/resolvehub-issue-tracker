package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryClassificationServiceTest {

    private final CategoryClassificationService service = new CategoryClassificationService();

    // --- SAFETY ---

    @ParameterizedTest
    @CsvSource({
            "Gas leak in kitchen, strong smell of gas near the oven",
            "Smoke alarm not working, fire safety hazard in corridor",
            "Carbon monoxide detector beeping, possible CO leak"
    })
    void safetyKeywordsReturnSafety(String title, String description) {
        assertEquals(IncidentCategory.SAFETY, service.classify(title, description));
    }

    // --- MAINTENANCE ---

    @ParameterizedTest
    @CsvSource({
            "Boiler broken, no hot water since yesterday morning",
            "Leak under bathroom sink, pipe joint is dripping",
            "Washing machine not working, stops mid-cycle",
            "Mould on bedroom ceiling, damp patch spreading",
            "Toilet keeps running, flush mechanism broken"
    })
    void maintenanceKeywordsReturnMaintenance(String title, String description) {
        assertEquals(IncidentCategory.MAINTENANCE, service.classify(title, description));
    }

    // --- NOISE ---

    @ParameterizedTest
    @CsvSource({
            "Loud music from Flat C305, playing after midnight every night",
            "Construction noise, drilling next door during quiet hours",
            "Dog barking constantly, neighbour's dog won't stop"
    })
    void noiseKeywordsReturnNoise(String title, String description) {
        assertEquals(IncidentCategory.NOISE, service.classify(title, description));
    }

    // --- INTERNET ---

    @ParameterizedTest
    @CsvSource({
            "WiFi not working, no internet connection in Room B12",
            "Broadband outage, router lights flashing red",
            "Slow speed on ethernet, connection dropping"
    })
    void internetKeywordsReturnInternet(String title, String description) {
        assertEquals(IncidentCategory.INTERNET, service.classify(title, description));
    }

    // --- BILLING ---

    @ParameterizedTest
    @CsvSource({
            "Electricity bill too high, overcharge on last invoice",
            "Rent payment not received, direct debit failed",
            "Need a refund, wrong charge on my account balance"
    })
    void billingKeywordsReturnBilling(String title, String description) {
        assertEquals(IncidentCategory.BILLING, service.classify(title, description));
    }

    // --- DEPOSIT ---

    @ParameterizedTest
    @CsvSource({
            "Deposit return dispute, unfair deduction for carpet",
            "End of tenancy checkout inspection, damage charge disagreement",
            "Bond not returned, deposit dispute with landlord"
    })
    void depositKeywordsReturnDeposit(String title, String description) {
        assertEquals(IncidentCategory.DEPOSIT, service.classify(title, description));
    }

    // --- CLEANING ---

    @ParameterizedTest
    @CsvSource({
            "Communal hallway dirty, not cleaned in weeks",
            "Pest infestation, cockroach spotted in kitchen",
            "Rubbish piling up, bin area not collected"
    })
    void cleaningKeywordsReturnCleaning(String title, String description) {
        assertEquals(IncidentCategory.CLEANING, service.classify(title, description));
    }

    // --- ACCESS ---

    @ParameterizedTest
    @CsvSource({
            "Lost key, locked out of my flat",
            "Access card not working, fob won't open security door",
            "Intercom broken, buzzer not responding for visitors"
    })
    void accessKeywordsReturnAccess(String title, String description) {
        assertEquals(IncidentCategory.ACCESS, service.classify(title, description));
    }

    // --- OTHER (fallback) ---

    @Test
    void noMatchReturnsOther() {
        assertEquals(IncidentCategory.OTHER, service.classify("General enquiry", "When does the tenancy end"));
    }

    // --- Ambiguous input picks category with most keyword hits ---

    @Test
    void ambiguousInputPicksHighestMatchCount() {
        IncidentCategory result = service.classify(
                "Broken lock on front door",
                "Cannot secure the door, lock is damaged, need repair of door mechanism"
        );
        assertEquals(IncidentCategory.MAINTENANCE, result);
    }

    // --- Case insensitive ---

    @Test
    void caseInsensitiveMatching() {
        assertEquals(IncidentCategory.SAFETY, service.classify("GAS LEAK", "STRONG SMELL OF GAS"));
    }
}
