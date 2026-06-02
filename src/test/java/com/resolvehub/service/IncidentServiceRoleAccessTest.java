package com.resolvehub.service;

import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.AccessDeniedException;
import com.resolvehub.repository.AuditLogRepository;
import com.resolvehub.repository.IncidentRepository;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests for role-based access control on IncidentService.
 *
 * <p>Verifies the access matrix:</p>
 * <ul>
 *   <li>USER can only read own incidents</li>
 *   <li>AGENT can only read assigned incidents</li>
 *   <li>MANAGER/ADMIN can read all incidents</li>
 *   <li>Unauthorised access returns 403 Forbidden</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IncidentService — role-based access control")
class IncidentServiceRoleAccessTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private UserRepository userRepository;
    @Mock private StatusTransitionValidator statusTransitionValidator;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private IncidentClassificationService classificationService;
    @Mock private CategoryClassificationService categoryClassificationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private IncidentService incidentService;

    private UUID incidentId;
    private User creator;
    private User agent;
    private User otherUser;
    private Incident incident;

    @BeforeEach
    void setUp() {
        incidentId = UUID.randomUUID();

        creator = User.builder().name("Alice").email("alice@tenant.io")
                .passwordHash("encoded").role(Role.USER).enabled(true).build();
        creator.setId(UUID.randomUUID());
        creator.setCreatedAt(LocalDateTime.now());
        creator.setUpdatedAt(LocalDateTime.now());

        agent = User.builder().name("Bob").email("bob@property.io")
                .passwordHash("encoded").role(Role.AGENT).enabled(true).build();
        agent.setId(UUID.randomUUID());
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());

        otherUser = User.builder().name("Eve").email("eve@tenant.io")
                .passwordHash("encoded").role(Role.USER).enabled(true).build();
        otherUser.setId(UUID.randomUUID());
        otherUser.setCreatedAt(LocalDateTime.now());
        otherUser.setUpdatedAt(LocalDateTime.now());

        incident = Incident.builder()
                .title("Test incident")
                .description("Test description")
                .category(IncidentCategory.MAINTENANCE)
                .priority(Priority.MEDIUM)
                .status(IncidentStatus.ASSIGNED)
                .createdBy(creator)
                .assignedTo(agent)
                .build();
        incident.setId(incidentId);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
    }

    private CustomUserDetails principalFor(User user) {
        return new CustomUserDetails(user);
    }

    @Nested
    @DisplayName("Read access")
    class ReadAccess {

        @Test
        @DisplayName("creator (USER) can read own incident")
        void creatorCanRead() {
            assertDoesNotThrow(() ->
                    incidentService.findById(incidentId, principalFor(creator)));
        }

        @Test
        @DisplayName("assigned agent can read incident")
        void assignedAgentCanRead() {
            assertDoesNotThrow(() ->
                    incidentService.findById(incidentId, principalFor(agent)));
        }

        @Test
        @DisplayName("other user CANNOT read someone else's incident")
        void otherUserCannotRead() {
            assertThrows(AccessDeniedException.class, () ->
                    incidentService.findById(incidentId, principalFor(otherUser)));
        }

        @Test
        @DisplayName("MANAGER can read any incident")
        void managerCanReadAny() {
            User manager = User.builder().name("Carol").email("carol@property.io")
                    .passwordHash("encoded").role(Role.MANAGER).enabled(true).build();
            manager.setId(UUID.randomUUID());

            assertDoesNotThrow(() ->
                    incidentService.findById(incidentId, principalFor(manager)));
        }

        @Test
        @DisplayName("ADMIN can read any incident")
        void adminCanReadAny() {
            User admin = User.builder().name("Admin").email("admin@property.io")
                    .passwordHash("encoded").role(Role.ADMIN).enabled(true).build();
            admin.setId(UUID.randomUUID());

            assertDoesNotThrow(() ->
                    incidentService.findById(incidentId, principalFor(admin)));
        }
    }
}
