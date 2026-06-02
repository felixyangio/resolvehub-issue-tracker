package com.resolvehub.dto.response;

import com.resolvehub.entity.Attachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AttachmentResponse {

    private final UUID id;
    private final String originalName;
    private final String contentType;
    private final long fileSize;
    private final String uploadedByName;
    private final LocalDateTime createdAt;

    public static AttachmentResponse fromEntity(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId())
                .originalName(a.getOriginalName())
                .contentType(a.getContentType())
                .fileSize(a.getFileSize())
                .uploadedByName(a.getUploadedBy().getName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
