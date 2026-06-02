package com.resolvehub.service;

import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.enums.IncidentCategory;
import com.resolvehub.enums.IncidentStatus;
import com.resolvehub.enums.Priority;
import com.resolvehub.enums.Role;
import com.resolvehub.exception.BadRequestException;
import com.resolvehub.repository.AttachmentRepository;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for AttachmentService — validates security controls on file uploads.
 *
 * <p>Focuses on rejection scenarios:</p>
 * <ul>
 *   <li>Empty files</li>
 *   <li>Oversized files</li>
 *   <li>Disallowed MIME types (e.g. executables)</li>
 *   <li>Exceeding attachment count limit</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttachmentService — upload validation")
class AttachmentServiceTest {

    @Mock private AttachmentRepository attachmentRepository;
    @Mock private IncidentService incidentService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    private UUID incidentId;
    private Incident incident;
    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "uploadDir", "/tmp/test-uploads");
        ReflectionTestUtils.setField(attachmentService, "maxSizeMb", 5);

        incidentId = UUID.randomUUID();

        User creator = User.builder().name("Alice").email("alice@tenant.io")
                .passwordHash("encoded").role(Role.USER).enabled(true).build();
        creator.setId(UUID.randomUUID());
        creator.setCreatedAt(LocalDateTime.now());
        creator.setUpdatedAt(LocalDateTime.now());

        incident = Incident.builder()
                .title("Test").description("Desc")
                .category(IncidentCategory.MAINTENANCE).priority(Priority.MEDIUM)
                .status(IncidentStatus.NEW).createdBy(creator).build();
        incident.setId(incidentId);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        principal = new CustomUserDetails(creator);
    }

    @Test
    @DisplayName("should reject empty files")
    void rejectsEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        when(incidentService.getIncidentOrThrow(incidentId)).thenReturn(incident);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attachmentService.upload(incidentId, file, principal));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("should reject files exceeding size limit")
    void rejectsOversizedFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024); // 10 MB > 5 MB limit

        when(incidentService.getIncidentOrThrow(incidentId)).thenReturn(incident);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attachmentService.upload(incidentId, file, principal));
        assertTrue(ex.getMessage().contains("maximum size"));
    }

    @Test
    @DisplayName("should reject disallowed file types (executables)")
    void rejectsExecutableFileType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/x-executable");

        when(incidentService.getIncidentOrThrow(incidentId)).thenReturn(incident);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attachmentService.upload(incidentId, file, principal));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("should reject when max attachments per incident is reached")
    void rejectsWhenMaxAttachmentsReached() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/png");

        when(incidentService.getIncidentOrThrow(incidentId)).thenReturn(incident);
        when(attachmentRepository.countByIncidentId(incidentId)).thenReturn(10L); // already at max

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attachmentService.upload(incidentId, file, principal));
        assertTrue(ex.getMessage().contains("Maximum"));
    }
}
