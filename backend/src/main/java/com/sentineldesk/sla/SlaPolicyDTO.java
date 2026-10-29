package com.sentineldesk.sla;

import com.sentineldesk.ticket.TicketPriority;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SlaPolicyDTO {
    private Long id;
    private String name;
    private TicketPriority priority;
    private BigDecimal firstResponseHours;
    private BigDecimal resolutionHours;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;

    public SlaPolicyDTO() {}

    public SlaPolicyDTO(SlaPolicy policy) {
        this.id = policy.getId();
        this.name = policy.getName();
        this.priority = policy.getPriority();
        this.firstResponseHours = policy.getFirstResponseHours();
        this.resolutionHours = policy.getResolutionHours();
        this.description = policy.getDescription();
        this.isActive = policy.isActive();
        this.createdAt = policy.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public BigDecimal getFirstResponseHours() {
        return firstResponseHours;
    }

    public void setFirstResponseHours(BigDecimal firstResponseHours) {
        this.firstResponseHours = firstResponseHours;
    }

    public BigDecimal getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(BigDecimal resolutionHours) {
        this.resolutionHours = resolutionHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
