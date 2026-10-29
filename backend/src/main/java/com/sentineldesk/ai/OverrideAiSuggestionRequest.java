package com.sentineldesk.ai;

import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OverrideAiSuggestionRequest {
    @NotNull(message = "Category is required")
    private TicketCategory newCategory;

    @NotNull(message = "Priority is required")
    private TicketPriority newPriority;

    private Long newTeamId;

    @NotBlank(message = "Override reason is mandatory")
    private String reason;

    public OverrideAiSuggestionRequest() {}

    public TicketCategory getNewCategory() {
        return newCategory;
    }

    public void setNewCategory(TicketCategory newCategory) {
        this.newCategory = newCategory;
    }

    public TicketPriority getNewPriority() {
        return newPriority;
    }

    public void setNewPriority(TicketPriority newPriority) {
        this.newPriority = newPriority;
    }

    public Long getNewTeamId() {
        return newTeamId;
    }

    public void setNewTeamId(Long newTeamId) {
        this.newTeamId = newTeamId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
