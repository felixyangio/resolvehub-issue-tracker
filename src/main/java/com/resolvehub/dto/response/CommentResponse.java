package com.resolvehub.dto.response;

import com.resolvehub.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {

    private final UUID id;
    private final String content;
    private final UUID authorId;
    private final String authorName;
    private final String authorRole;
    private final LocalDateTime createdAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .authorRole(comment.getAuthor().getRole().name())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
