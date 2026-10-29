package com.sentineldesk.escalation;

import com.sentineldesk.user.UserDTO;
import java.time.LocalDateTime;

public class EscalationEventDTO {
    private Long id;
    private Long ticketId;
    private String ticketNumber;
    private String ticketTitle;
    private String ticketPriority;
    private EscalationLevel escalationLevel;
    private String reason;
    private EscalationStatus status;
    private UserDTO escalatedTo;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public EscalationEventDTO() {}

    public EscalationEventDTO(EscalationEvent event) {
        this.id = event.getId();
        this.ticketId = event.getTicket().getId();
        this.ticketNumber = event.getTicket().getTicketNumber();
        this.ticketTitle = event.getTicket().getTitle();
        this.ticketPriority = event.getTicket().getPriority().name();
        this.escalationLevel = event.getEscalationLevel();
        this.reason = event.getReason();
        this.status = event.getStatus();
        this.escalatedTo = UserDTO.fromEntity(event.getEscalatedTo());
        this.createdAt = event.getCreatedAt();
        this.resolvedAt = event.getResolvedAt();
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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketPriority() {
        return ticketPriority;
    }

    public void setTicketPriority(String ticketPriority) {
        this.ticketPriority = ticketPriority;
    }

    public EscalationLevel getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(EscalationLevel escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public EscalationStatus getStatus() {
        return status;
    }

    public void setStatus(EscalationStatus status) {
        this.status = status;
    }

    public UserDTO getEscalatedTo() {
        return escalatedTo;
    }

    public void setEscalatedTo(UserDTO escalatedTo) {
        this.escalatedTo = escalatedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
