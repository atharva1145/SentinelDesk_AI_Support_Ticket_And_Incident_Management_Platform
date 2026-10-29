package com.sentineldesk.sla;

import com.sentineldesk.ticket.Ticket;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_trackers")
public class SlaTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Column(name = "first_response_deadline", nullable = false)
    private LocalDateTime firstResponseDeadline;

    @Column(name = "first_response_met")
    private Boolean firstResponseMet = false;

    @Column(name = "first_response_breached")
    private Boolean firstResponseBreached = false;

    @Column(name = "resolution_deadline", nullable = false)
    private LocalDateTime resolutionDeadline;

    @Column(name = "resolution_met")
    private Boolean resolutionMet = false;

    @Column(name = "resolution_breached")
    private Boolean resolutionBreached = false;

    @Column(name = "is_escalated")
    private Boolean isEscalated = false;

    @Column(name = "warning_issued")
    private Boolean warningIssued = false;

    @Column(name = "is_paused")
    private Boolean isPaused = false;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "total_paused_minutes")
    private Integer totalPausedMinutes = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SlaTracker() {}

    public SlaTracker(Ticket ticket, SlaPolicy policy, LocalDateTime firstResponseDeadline, LocalDateTime resolutionDeadline) {
        this.ticket = ticket;
        this.policy = policy;
        this.firstResponseDeadline = firstResponseDeadline;
        this.resolutionDeadline = resolutionDeadline;
        this.firstResponseMet = false;
        this.firstResponseBreached = false;
        this.resolutionMet = false;
        this.resolutionBreached = false;
        this.isEscalated = false;
        this.warningIssued = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public SlaPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(SlaPolicy policy) {
        this.policy = policy;
    }

    public LocalDateTime getFirstResponseDeadline() {
        return firstResponseDeadline;
    }

    public void setFirstResponseDeadline(LocalDateTime firstResponseDeadline) {
        this.firstResponseDeadline = firstResponseDeadline;
    }

    public Boolean getFirstResponseMet() {
        return firstResponseMet;
    }

    public void setFirstResponseMet(Boolean firstResponseMet) {
        this.firstResponseMet = firstResponseMet;
    }

    public Boolean getFirstResponseBreached() {
        return firstResponseBreached;
    }

    public void setFirstResponseBreached(Boolean firstResponseBreached) {
        this.firstResponseBreached = firstResponseBreached;
    }

    public LocalDateTime getResolutionDeadline() {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(LocalDateTime resolutionDeadline) {
        this.resolutionDeadline = resolutionDeadline;
    }

    public Boolean getResolutionMet() {
        return resolutionMet;
    }

    public void setResolutionMet(Boolean resolutionMet) {
        this.resolutionMet = resolutionMet;
    }

    public Boolean getResolutionBreached() {
        return resolutionBreached;
    }

    public void setResolutionBreached(Boolean resolutionBreached) {
        this.resolutionBreached = resolutionBreached;
    }

    public Boolean getIsEscalated() {
        return isEscalated;
    }

    public void setIsEscalated(Boolean isEscalated) {
        this.isEscalated = isEscalated;
    }

    public Boolean getWarningIssued() {
        return warningIssued;
    }

    public void setWarningIssued(Boolean warningIssued) {
        this.warningIssued = warningIssued;
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

    public Boolean getIsPaused() {
        return isPaused != null && isPaused;
    }

    public void setIsPaused(Boolean paused) {
        this.isPaused = paused;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Integer getTotalPausedMinutes() {
        return totalPausedMinutes != null ? totalPausedMinutes : 0;
    }

    public void setTotalPausedMinutes(Integer totalPausedMinutes) {
        this.totalPausedMinutes = totalPausedMinutes;
    }

    public void pause(LocalDateTime now) {
        if (!Boolean.TRUE.equals(this.isPaused)) {
            this.isPaused = true;
            this.pausedAt = now != null ? now : LocalDateTime.now();
        }
    }

    public void resume(LocalDateTime now) {
        if (Boolean.TRUE.equals(this.isPaused) && this.pausedAt != null) {
            LocalDateTime effectiveNow = now != null ? now : LocalDateTime.now();
            long pausedMins = java.time.Duration.between(this.pausedAt, effectiveNow).toMinutes();
            if (pausedMins > 0) {
                this.totalPausedMinutes = (this.totalPausedMinutes != null ? this.totalPausedMinutes : 0) + (int) pausedMins;
                // Extend resolution deadline by the paused duration
                if (this.resolutionDeadline != null) {
                    this.resolutionDeadline = this.resolutionDeadline.plusMinutes(pausedMins);
                    if (this.ticket != null) {
                        this.ticket.setSlaDeadline(this.resolutionDeadline);
                    }
                }
            }
            this.isPaused = false;
            this.pausedAt = null;
        }
    }
}
