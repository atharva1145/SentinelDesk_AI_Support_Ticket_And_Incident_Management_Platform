package com.sentineldesk.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<TicketComment, Long> {
    @Query("SELECT c FROM TicketComment c JOIN FETCH c.author WHERE c.ticket.id = :ticketId ORDER BY c.createdAt ASC")
    List<TicketComment> findAllByTicketIdWithAuthor(Long ticketId);

    @Query("SELECT c FROM TicketComment c JOIN FETCH c.author WHERE c.ticket.id = :ticketId AND c.isInternal = false ORDER BY c.createdAt ASC")
    List<TicketComment> findPublicByTicketIdWithAuthor(Long ticketId);
}
