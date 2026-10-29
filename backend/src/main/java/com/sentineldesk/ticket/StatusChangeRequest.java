package com.sentineldesk.ticket;

import jakarta.validation.constraints.NotNull;

public class StatusChangeRequest {
    @NotNull(message = "New status is required")
    private TicketStatus status;

    private String reason;
    private String resolutionSummary;

    public StatusChangeRequest() {}

    public StatusChangeRequest(TicketStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }
}
