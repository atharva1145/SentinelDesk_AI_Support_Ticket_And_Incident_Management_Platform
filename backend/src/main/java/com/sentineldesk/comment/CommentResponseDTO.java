package com.sentineldesk.comment;

import com.sentineldesk.user.UserDTO;
import java.time.LocalDateTime;

public class CommentResponseDTO {
    private Long id;
    private Long ticketId;
    private UserDTO author;
    private String content;
    private boolean isInternal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDTO() {}

    public CommentResponseDTO(TicketComment comment) {
        this.id = comment.getId();
        this.ticketId = comment.getTicket().getId();
        this.author = UserDTO.fromEntity(comment.getAuthor());
        this.content = comment.getContent();
        this.isInternal = comment.isInternal();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
