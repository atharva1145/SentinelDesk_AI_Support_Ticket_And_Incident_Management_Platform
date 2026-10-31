package com.sentineldesk.attachment;

import java.time.LocalDateTime;

public class AttachmentDTO {

    private Long id;
    private Long ticketId;
    private Long incidentId;
    private String filename;
    private Long fileSize;
    private String contentType;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
    private String downloadUrl;

    public AttachmentDTO() {}

    public AttachmentDTO(Attachment attachment) {
        this.id = attachment.getId();
        this.ticketId = attachment.getTicket() != null ? attachment.getTicket().getId() : null;
        this.incidentId = attachment.getIncident() != null ? attachment.getIncident().getId() : null;
        this.filename = attachment.getFilename();
        this.fileSize = attachment.getFileSize();
        this.contentType = attachment.getContentType();
        if (attachment.getUploadedBy() != null) {
            this.uploadedById = attachment.getUploadedBy().getId();
            this.uploadedByName = attachment.getUploadedBy().getFullName();
        }
        this.createdAt = attachment.getCreatedAt();
        this.downloadUrl = "/api/v1/attachments/" + attachment.getId() + "/download";
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

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
