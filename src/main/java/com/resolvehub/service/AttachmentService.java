package com.resolvehub.service;

import com.resolvehub.dto.response.AttachmentResponse;
import com.resolvehub.entity.Attachment;
import com.resolvehub.entity.Incident;
import com.resolvehub.entity.User;
import com.resolvehub.exception.BadRequestException;
import com.resolvehub.exception.ResourceNotFoundException;
import com.resolvehub.repository.AttachmentRepository;
import com.resolvehub.repository.UserRepository;
import com.resolvehub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing file attachments on incidents.
 *
 * <p>Security measures:</p>
 * <ul>
 *   <li>File type whitelist — only images, PDFs, and common docs allowed</li>
 *   <li>Max file size enforced (5 MB default, configurable)</li>
 *   <li>Max 10 attachments per incident</li>
 *   <li>Files stored with UUID names to prevent path traversal</li>
 *   <li>Access control — same rules as incident read access</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "text/plain", "text/csv",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final int MAX_ATTACHMENTS_PER_INCIDENT = 10;

    private final AttachmentRepository attachmentRepository;
    private final IncidentService incidentService;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:5}")
    private int maxSizeMb;

    @Transactional
    public AttachmentResponse upload(UUID incidentId, MultipartFile file, CustomUserDetails userDetails) {
        // Access check (reuses incident read-access logic)
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > (long) maxSizeMb * 1024 * 1024) {
            throw new BadRequestException("File exceeds maximum size of " + maxSizeMb + " MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException("File type not allowed. Accepted: images, PDF, Word, CSV, TXT");
        }
        if (attachmentRepository.countByIncidentId(incidentId) >= MAX_ATTACHMENTS_PER_INCIDENT) {
            throw new BadRequestException("Maximum " + MAX_ATTACHMENTS_PER_INCIDENT + " attachments per case");
        }

        // Store file
        String storedName = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new BadRequestException("Failed to store file");
        }

        // Save metadata
        User uploader = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Attachment attachment = Attachment.builder()
                .incident(incident)
                .uploadedBy(uploader)
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(contentType)
                .fileSize(file.getSize())
                .build();

        return AttachmentResponse.fromEntity(attachmentRepository.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listByIncident(UUID incidentId, CustomUserDetails userDetails) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);
        return attachmentRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource download(UUID incidentId, UUID attachmentId, CustomUserDetails userDetails) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        incidentService.checkReadAccess(incident, userDetails);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredName());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("File not found on disk");
            }
            return resource;
        } catch (IOException e) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    public String getContentType(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(Attachment::getContentType)
                .orElse("application/octet-stream");
    }

    public String getOriginalName(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(Attachment::getOriginalName)
                .orElse("download");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
