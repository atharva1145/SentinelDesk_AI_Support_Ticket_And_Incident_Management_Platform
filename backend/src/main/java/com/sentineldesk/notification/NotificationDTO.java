package com.sentineldesk.notification;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private String title;
    private String message;
    private String link;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Notification notif) {
        this.id = notif.getId();
        this.recipientId = notif.getRecipient().getId();
        this.title = notif.getTitle();
        this.message = notif.getMessage();
        this.link = notif.getLink();
        this.isRead = notif.isRead();
        this.createdAt = notif.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
