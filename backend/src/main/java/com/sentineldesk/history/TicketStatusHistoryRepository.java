package com.sentineldesk.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long> {
    @Query("SELECT tsh FROM TicketStatusHistory tsh LEFT JOIN FETCH tsh.changedBy WHERE tsh.ticket.id = :ticketId ORDER BY tsh.createdAt ASC")
    List<TicketStatusHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
