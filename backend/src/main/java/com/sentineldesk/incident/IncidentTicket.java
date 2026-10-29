package com.sentineldesk.incident;

import com.sentineldesk.ticket.Ticket;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "incident_tickets")
@IdClass(IncidentTicket.IncidentTicketId.class)
public class IncidentTicket {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IncidentTicket() {}

    public IncidentTicket(Incident incident, Ticket ticket) {
        this.incident = incident;
        this.ticket = ticket;
        this.createdAt = LocalDateTime.now();
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class IncidentTicketId implements Serializable {
        private Long incident;
        private Long ticket;

        public IncidentTicketId() {}

        public IncidentTicketId(Long incident, Long ticket) {
            this.incident = incident;
            this.ticket = ticket;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IncidentTicketId that = (IncidentTicketId) o;
            return Objects.equals(incident, that.incident) && Objects.equals(ticket, that.ticket);
        }

        @Override
        public int hashCode() {
            return Objects.hash(incident, ticket);
        }
    }
}
