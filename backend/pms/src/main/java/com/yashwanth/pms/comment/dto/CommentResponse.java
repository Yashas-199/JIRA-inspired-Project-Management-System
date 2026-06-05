package com.yashwanth.pms.comment.dto;

import com.yashwanth.pms.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentResponse {

    private UUID id;
    private String content;
    private UUID authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private String visibility;

    public static CommentResponse from(Comment comment) {
        CommentResponse r = new CommentResponse();
        r.id = comment.getId();
        r.content = comment.getContent();
        r.authorId = comment.getAuthor().getId();
        r.authorName = comment.getAuthor().getName();
        r.createdAt = comment.getCreatedAt();
        r.visibility = comment.getVisibility() != null ? comment.getVisibility().name() : null;
        return r;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getVisibility() {
        return visibility;
    }
}
