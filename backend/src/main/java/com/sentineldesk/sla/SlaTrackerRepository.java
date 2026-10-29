package com.sentineldesk.sla;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaTrackerRepository extends JpaRepository<SlaTracker, Long> {
    Optional<SlaTracker> findByTicketId(Long ticketId);

    @Query("SELECT st FROM SlaTracker st JOIN FETCH st.ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<SlaTracker> findActiveTrackersWithTickets();

    @Query("SELECT st FROM SlaTracker st JOIN FETCH st.ticket t " +
           "WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "AND st.resolutionDeadline < :now AND st.resolutionBreached = false")
    List<SlaTracker> findBreachedResolutionTrackers(LocalDateTime now);

    @Query("SELECT st FROM SlaTracker st JOIN FETCH st.ticket t " +
           "WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "AND st.firstResponseDeadline < :now AND st.firstResponseBreached = false AND st.firstResponseMet = false")
    List<SlaTracker> findBreachedFirstResponseTrackers(LocalDateTime now);
}
