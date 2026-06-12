package com.resolvehub.service;

import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncidentClassificationServiceTest {

    private final IncidentClassificationService service = new IncidentClassificationService();

    // --- Category-based classification ---

    @Test
    void safetyCategoryReturnsHigh() {
        Priority result = service.classify("Broken smoke alarm", "Alarm not sounding when tested", IncidentCategory.SAFETY);
        assertEquals(Priority.HIGH, result);
    }

    @Test
    void safetyWithGasLeakKeywordIsCritical() {
        // Gas leak matches CRITICAL_KEYWORDS — always escalates to CRITICAL regardless of category
        Priority result = service.classify("Gas leak in kitchen", "Strong smell of gas", IncidentCategory.SAFETY);
        assertEquals(Priority.CRITICAL, result);
    }

    @Test
    void safetyCategoryWithoutCriticalKeywordsIsHigh() {
        // SAFETY category without critical keywords defaults to HIGH
        Priority result = service.classify("Broken window latch", "Window does not lock properly", IncidentCategory.SAFETY);
        assertEquals(Priority.HIGH, result);
    }

    // --- Critical keywords (accommodation emergencies) ---

    @ParameterizedTest
    @CsvSource({
            "Flood in bathroom, water coming through ceiling",
            "Gas leak in Flat B204, strong smell in hallway",
            "No heating in building, radiators cold in all flats",
            "Burst pipe under kitchen sink, water spraying everywhere",
            "Gas smell in communal area, residents evacuating"
    })
    void criticalKeywordsReturnCritical(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.MAINTENANCE);
        assertEquals(Priority.CRITICAL, result);
    }

    // --- High keywords (urgent repairs) ---

    @ParameterizedTest
    @CsvSource({
            "Water leak under bathroom sink, constant dripping from pipe joint",
            "Broken lock on front door, deadbolt does not engage",
            "Boiler fault, no hot water since yesterday",
            "Mould appearing near bedroom window, black patches spreading",
            "Washing machine not working, stops mid-cycle with error"
    })
    void highKeywordsReturnHigh(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.MAINTENANCE);
        assertEquals(Priority.HIGH, result);
    }

    // --- Low keywords (minor requests) ---

    @ParameterizedTest
    @CsvSource({
            "Lightbulb replacement in hallway, bulb gone in corridor",
            "Minor scratch on kitchen worktop, cosmetic damage only",
            "Bin collection query, when are bins collected",
            "Paint peeling on window frame, small area of flaking"
    })
    void lowKeywordsReturnLow(String title, String description) {
        Priority result = service.classify(title, description, IncidentCategory.MAINTENANCE);
        assertEquals(Priority.LOW, result);
    }

    // --- Default to MEDIUM ---

    @Test
    void noMatchingKeywordsReturnMedium() {
        Priority result = service.classify("Tap dripping slowly", "Kitchen tap drips occasionally", IncidentCategory.MAINTENANCE);
        assertEquals(Priority.MEDIUM, result);
    }

    // --- Precedence ---

    @Test
    void criticalTakesPrecedenceOverHigh() {
        Priority result = service.classify("Burst pipe causing leak", "Water flooding hallway", IncidentCategory.MAINTENANCE);
        assertEquals(Priority.CRITICAL, result);
    }

    @Test
    void caseInsensitiveMatching() {
        Priority result = service.classify("NO HEATING", "RADIATORS COLD", IncidentCategory.MAINTENANCE);
        assertEquals(Priority.CRITICAL, result);
    }

    // --- Domain-specific category tests ---

    @Test
    void noiseComplaintDefaultsToMedium() {
        Priority result = service.classify("Loud music from Flat C305", "Happening after midnight", IncidentCategory.NOISE);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void internetIssueDefaultsToMedium() {
        Priority result = service.classify("Wi-Fi unavailable in Room C312", "No wireless signal detected", IncidentCategory.INTERNET);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void depositDisputeDefaultsToMedium() {
        Priority result = service.classify("Deposit deduction dispute", "Charged for carpet cleaning but have receipt", IncidentCategory.DEPOSIT);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void cleaningRequestDefaultsToMedium() {
        Priority result = service.classify("Communal hallway dirty", "Not cleaned for two weeks", IncidentCategory.CLEANING);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void accessIssueWithBrokenLockReturnsHigh() {
        Priority result = service.classify("Broken lock on room door", "Cannot secure room", IncidentCategory.ACCESS);
        assertEquals(Priority.HIGH, result);
    }

    @Test
    void billingQueryDefaultsToMedium() {
        Priority result = service.classify("Electricity charge too high", "Bill seems incorrect", IncidentCategory.BILLING);
        assertEquals(Priority.MEDIUM, result);
    }

    @Test
    void otherCategoryDefaultsToMedium() {
        Priority result = service.classify("General enquiry", "When does the tenancy end", IncidentCategory.OTHER);
        assertEquals(Priority.MEDIUM, result);
    }
}
