package com.sentineldesk.sla;

import com.sentineldesk.ticket.TicketPriority;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TicketPriority priority;

    @Column(name = "first_response_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal firstResponseHours;

    @Column(name = "resolution_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal resolutionHours;

    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_business_hours", nullable = false)
    private boolean isBusinessHours = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SlaPolicy() {}

    public SlaPolicy(String name, TicketPriority priority, BigDecimal firstResponseHours, BigDecimal resolutionHours, String description) {
        this.name = name;
        this.priority = priority;
        this.firstResponseHours = firstResponseHours;
        this.resolutionHours = resolutionHours;
        this.description = description;
        this.isActive = true;
        this.isBusinessHours = priority != TicketPriority.P1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SlaPolicy(String name, TicketPriority priority, BigDecimal firstResponseHours, BigDecimal resolutionHours, String description, boolean isBusinessHours) {
        this(name, priority, firstResponseHours, resolutionHours, description);
        this.isBusinessHours = isBusinessHours;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public boolean isBusinessHours() {
        return isBusinessHours;
    }

    public void setBusinessHours(boolean businessHours) {
        isBusinessHours = businessHours;
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
