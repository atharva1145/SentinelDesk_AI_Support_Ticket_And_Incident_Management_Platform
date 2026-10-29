package com.sentineldesk.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAiSuggestionRepository extends JpaRepository<TicketAiSuggestion, Long> {
    List<TicketAiSuggestion> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    Optional<TicketAiSuggestion> findFirstByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
