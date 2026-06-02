package com.resolvehub.controller;

import com.resolvehub.dto.response.AttachmentResponse;
import com.resolvehub.security.CustomUserDetails;
import com.resolvehub.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents/{incidentId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable UUID incidentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attachmentService.upload(incidentId, file, userDetails));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> list(
            @PathVariable UUID incidentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(attachmentService.listByIncident(incidentId, userDetails));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(
            @PathVariable UUID incidentId,
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Resource resource = attachmentService.download(incidentId, attachmentId, userDetails);
        String contentType = attachmentService.getContentType(attachmentId);
        String originalName = attachmentService.getOriginalName(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalName + "\"")
                .body(resource);
    }
}
