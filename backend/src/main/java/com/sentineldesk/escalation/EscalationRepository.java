package com.sentineldesk.escalation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRepository extends JpaRepository<EscalationEvent, Long> {
    boolean existsByTicketIdAndEscalationLevel(Long ticketId, EscalationLevel escalationLevel);
    List<EscalationEvent> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    @Query("SELECT e FROM EscalationEvent e JOIN FETCH e.ticket t LEFT JOIN FETCH e.escalatedTo ORDER BY e.createdAt DESC")
    List<EscalationEvent> findAllWithDetails();

    long countByStatus(EscalationStatus status);
}
