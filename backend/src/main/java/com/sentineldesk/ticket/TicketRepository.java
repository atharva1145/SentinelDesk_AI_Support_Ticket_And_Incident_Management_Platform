package com.sentineldesk.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByRequesterId(Long requesterId, Pageable pageable);

    Page<Ticket> findByAssignedAgentId(Long agentId, Pageable pageable);

    Page<Ticket> findByAssignedTeamId(Long teamId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.assignedAgent IS NULL AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    Page<Ticket> findUnassignedActiveTickets(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent.id = :agentId AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    long countActiveTicketsByAgentId(@org.springframework.data.repository.query.Param("agentId") Long agentId);

    // Dashboard count queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    long countOpenTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent IS NULL AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    long countUnassignedTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority IN ('P1', 'P2') AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    long countCriticalAndHighTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED' AND t.resolvedAt >= :startOfDay")
    long countResolvedSince(LocalDateTime startOfDay);

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countByPriorityGroup();

    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countByCategoryGroup();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT t.assignedAgent.id, t.assignedAgent.fullName, COUNT(t) " +
           "FROM Ticket t WHERE t.assignedAgent IS NOT NULL AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "GROUP BY t.assignedAgent.id, t.assignedAgent.fullName")
    List<Object[]> countWorkloadByAgent();

    @Query("SELECT t.assignedTeam.id, t.assignedTeam.name, COUNT(t) " +
           "FROM Ticket t WHERE t.assignedTeam IS NOT NULL AND t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "GROUP BY t.assignedTeam.id, t.assignedTeam.name")
    List<Object[]> countWorkloadByTeam();
}
