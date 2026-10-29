package com.sentineldesk.sla;

import java.time.Duration;
import java.time.LocalDateTime;

public class SlaTrackerDTO {
    private Long id;
    private Long ticketId;
    private SlaPolicyDTO policy;
    private LocalDateTime firstResponseDeadline;
    private Boolean firstResponseMet;
    private Boolean firstResponseBreached;
    private LocalDateTime resolutionDeadline;
    private Boolean resolutionMet;
    private Boolean resolutionBreached;
    private Boolean isEscalated;
    private Boolean warningIssued;
    private Boolean isPaused;
    private LocalDateTime pausedAt;
    private Integer totalPausedMinutes;
    private Long remainingMinutes;
    private boolean isOverdue;

    public SlaTrackerDTO() {}

    public SlaTrackerDTO(SlaTracker tracker) {
        this.id = tracker.getId();
        this.ticketId = tracker.getTicket().getId();
        this.policy = new SlaPolicyDTO(tracker.getPolicy());
        this.firstResponseDeadline = tracker.getFirstResponseDeadline();
        this.firstResponseMet = tracker.getFirstResponseMet();
        this.firstResponseBreached = tracker.getFirstResponseBreached();
        this.resolutionDeadline = tracker.getResolutionDeadline();
        this.resolutionMet = tracker.getResolutionMet();
        this.resolutionBreached = tracker.getResolutionBreached();
        this.isEscalated = tracker.getIsEscalated();
        this.warningIssued = tracker.getWarningIssued();
        this.isPaused = tracker.getIsPaused();
        this.pausedAt = tracker.getPausedAt();
        this.totalPausedMinutes = tracker.getTotalPausedMinutes();

        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(tracker.getResolutionMet())) {
            this.remainingMinutes = 0L;
            this.isOverdue = false;
        } else {
            Duration duration = Duration.between(now, tracker.getResolutionDeadline());
            this.remainingMinutes = duration.toMinutes();
            this.isOverdue = duration.isNegative();
        }
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

    public SlaPolicyDTO getPolicy() {
        return policy;
    }

    public void setPolicy(SlaPolicyDTO policy) {
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

    public Long getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(Long remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }

    public Boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(Boolean isPaused) {
        this.isPaused = isPaused;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Integer getTotalPausedMinutes() {
        return totalPausedMinutes;
    }

    public void setTotalPausedMinutes(Integer totalPausedMinutes) {
        this.totalPausedMinutes = totalPausedMinutes;
    }
}
