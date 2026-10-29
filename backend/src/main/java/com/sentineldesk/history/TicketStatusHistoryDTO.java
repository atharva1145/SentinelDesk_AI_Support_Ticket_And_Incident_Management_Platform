package com.sentineldesk.history;

import com.sentineldesk.ticket.TicketStatus;
import com.sentineldesk.user.UserDTO;
import java.time.LocalDateTime;

public class TicketStatusHistoryDTO {
    private Long id;
    private Long ticketId;
    private UserDTO changedBy;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private String reason;
    private LocalDateTime createdAt;

    public TicketStatusHistoryDTO() {}

    public TicketStatusHistoryDTO(TicketStatusHistory tsh) {
        this.id = tsh.getId();
        this.ticketId = tsh.getTicket().getId();
        this.changedBy = UserDTO.fromEntity(tsh.getChangedBy());
        this.oldStatus = tsh.getOldStatus();
        this.newStatus = tsh.getNewStatus();
        this.reason = tsh.getReason();
        this.createdAt = tsh.getCreatedAt();
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

    public UserDTO getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UserDTO changedBy) {
        this.changedBy = changedBy;
    }

    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TicketStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
