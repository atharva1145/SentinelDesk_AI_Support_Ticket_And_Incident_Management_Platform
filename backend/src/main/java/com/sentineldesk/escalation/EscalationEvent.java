package com.sentineldesk.escalation;

import com.sentineldesk.sla.SlaTracker;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id", "escalation_level"})
})
public class EscalationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracker_id", nullable = false)
    private SlaTracker tracker;

    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_level", nullable = false)
    private EscalationLevel escalationLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationStatus status = EscalationStatus.TRIGGERED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_id")
    private User escalatedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public EscalationEvent() {}

    public EscalationEvent(Ticket ticket, SlaTracker tracker, EscalationLevel escalationLevel,
                           String reason, User escalatedTo) {
        this.ticket = ticket;
        this.tracker = tracker;
        this.escalationLevel = escalationLevel;
        this.reason = reason;
        this.status = EscalationStatus.TRIGGERED;
        this.escalatedTo = escalatedTo;
        this.createdAt = LocalDateTime.now();
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

    public SlaTracker getTracker() {
        return tracker;
    }

    public void setTracker(SlaTracker tracker) {
        this.tracker = tracker;
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

    public User getEscalatedTo() {
        return escalatedTo;
    }

    public void setEscalatedTo(User escalatedTo) {
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
