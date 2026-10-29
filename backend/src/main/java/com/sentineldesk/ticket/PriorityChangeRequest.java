package com.sentineldesk.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PriorityChangeRequest {
    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotBlank(message = "Reason for priority change is mandatory")
    private String reason;

    public PriorityChangeRequest() {}

    public PriorityChangeRequest(TicketPriority priority, String reason) {
        this.priority = priority;
        this.reason = reason;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
