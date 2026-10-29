package com.sentineldesk.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAssignmentHistoryRepository extends JpaRepository<TicketAssignmentHistory, Long> {
    @Query("SELECT tah FROM TicketAssignmentHistory tah " +
           "LEFT JOIN FETCH tah.changedBy " +
           "LEFT JOIN FETCH tah.oldAgent " +
           "LEFT JOIN FETCH tah.newAgent " +
           "LEFT JOIN FETCH tah.oldTeam " +
           "LEFT JOIN FETCH tah.newTeam " +
           "WHERE tah.ticket.id = :ticketId ORDER BY tah.createdAt ASC")
    List<TicketAssignmentHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
