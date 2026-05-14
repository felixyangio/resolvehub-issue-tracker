package com.resolvehub.config;

import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        log.info("Seeding demo data...");

        String hash = passwordEncoder.encode("password");

        User alice = userRepository.save(User.builder()
                .name("Alice Chen").email("alice@tenant.io")
                .passwordHash(hash).role(Role.USER).enabled(true).build());

        User bob = userRepository.save(User.builder()
                .name("Bob Torres").email("bob@property.io")
                .passwordHash(hash).role(Role.AGENT).enabled(true).build());

        User carol = userRepository.save(User.builder()
                .name("Carol Perry").email("carol@property.io")
                .passwordHash(hash).role(Role.MANAGER).enabled(true).build());

        userRepository.save(User.builder()
                .name("David Kim").email("david@property.io")
                .passwordHash(hash).role(Role.AGENT).enabled(true).build());

        userRepository.save(User.builder()
                .name("Admin User").email("admin@resolvehub.io")
                .passwordHash(hash).role(Role.ADMIN).enabled(true).build());

        incidentRepository.save(Incident.builder()
                .title("Heating not working in Flat B204")
                .description("The radiators in the living room and bedroom have been cold since Monday. Thermostat shows heating is on but nothing happens.")
                .category(IncidentCategory.MAINTENANCE).priority(Priority.CRITICAL)
                .status(IncidentStatus.IN_PROGRESS).createdBy(alice).assignedTo(bob).build());

        incidentRepository.save(Incident.builder()
                .title("Water leak under kitchen sink")
                .description("Slow drip from the U-bend pipe. Placed a bucket but it fills up daily.")
                .category(IncidentCategory.MAINTENANCE).priority(Priority.HIGH)
                .status(IncidentStatus.ASSIGNED).createdBy(alice).assignedTo(bob).build());

        incidentRepository.save(Incident.builder()
                .title("Excessive noise from Flat C301")
                .description("Loud music after 11pm on weeknights. Has been happening for two weeks.")
                .category(IncidentCategory.NOISE).priority(Priority.MEDIUM)
                .status(IncidentStatus.NEW).createdBy(alice).build());

        incidentRepository.save(Incident.builder()
                .title("WiFi intermittent in Block A")
                .description("Connection drops every 20-30 minutes throughout the day. Multiple residents affected.")
                .category(IncidentCategory.INTERNET).priority(Priority.HIGH)
                .status(IncidentStatus.NEW).createdBy(carol).build());

        incidentRepository.save(Incident.builder()
                .title("Deposit return query")
                .description("Moved out on 1st March, still waiting for deposit return. Flat was left in good condition.")
                .category(IncidentCategory.DEPOSIT).priority(Priority.LOW)
                .status(IncidentStatus.RESOLVED).createdBy(alice).assignedTo(bob).build());

        log.info("Seeded 5 users and 5 incidents");
    }
}
