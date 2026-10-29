package com.sentineldesk.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentTicketRepository extends JpaRepository<IncidentTicket, IncidentTicket.IncidentTicketId> {
    List<IncidentTicket> findByIncidentId(Long incidentId);
    List<IncidentTicket> findByTicketId(Long ticketId);

    @Query("SELECT it FROM IncidentTicket it JOIN FETCH it.ticket WHERE it.incident.id = :incidentId")
    List<IncidentTicket> findByIncidentIdWithTickets(Long incidentId);

    void deleteByIncidentIdAndTicketId(Long incidentId, Long ticketId);
}
