package com.sentineldesk.history;

import com.sentineldesk.team.Team;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_assignment_history")
public class TicketAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_agent_id")
    private User oldAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_agent_id")
    private User newAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_team_id")
    private Team oldTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_team_id")
    private Team newTeam;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TicketAssignmentHistory() {}

    public TicketAssignmentHistory(Ticket ticket, User changedBy, User oldAgent, User newAgent,
                                   Team oldTeam, Team newTeam, String reason) {
        this.ticket = ticket;
        this.changedBy = changedBy;
        this.oldAgent = oldAgent;
        this.newAgent = newAgent;
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
        this.reason = reason;
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

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public User getOldAgent() {
        return oldAgent;
    }

    public void setOldAgent(User oldAgent) {
        this.oldAgent = oldAgent;
    }

    public User getNewAgent() {
        return newAgent;
    }

    public void setNewAgent(User newAgent) {
        this.newAgent = newAgent;
    }

    public Team getOldTeam() {
        return oldTeam;
    }

    public void setOldTeam(Team oldTeam) {
        this.oldTeam = oldTeam;
    }

    public Team getNewTeam() {
        return newTeam;
    }

    public void setNewTeam(Team newTeam) {
        this.newTeam = newTeam;
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
