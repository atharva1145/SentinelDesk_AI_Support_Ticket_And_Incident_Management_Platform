package com.sentineldesk.routing;

import com.sentineldesk.common.exception.BadRequestException;
import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.team.TeamMember;
import com.sentineldesk.team.TeamMemberRepository;
import com.sentineldesk.ticket.AssignTicketRequest;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.ticket.TicketService;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TicketService ticketService;

    private final AtomicLong roundRobinCounter = new AtomicLong(0);

    public RoutingService(TicketRepository ticketRepository,
                          UserRepository userRepository,
                          TeamMemberRepository teamMemberRepository,
                          TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.ticketService = ticketService;
    }

    /**
     * Auto-assigns a ticket using smart routing strategies (LEAST_BUSY or ROUND_ROBIN).
     */
    @Transactional
    public AutoAssignResultDTO autoAssignTicket(Long ticketId, String rawStrategy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        String strategy = (rawStrategy != null && rawStrategy.equalsIgnoreCase("ROUND_ROBIN"))
                ? "ROUND_ROBIN" : "LEAST_BUSY";

        List<User> candidateAgents = getCandidateAgents(ticket);
        if (candidateAgents.isEmpty()) {
            throw new BadRequestException("No active support agents or managers available for auto-assignment.");
        }

        User chosenAgent;
        long activeTickets;

        if ("ROUND_ROBIN".equals(strategy)) {
            int index = (int) (Math.abs(roundRobinCounter.getAndIncrement()) % candidateAgents.size());
            chosenAgent = candidateAgents.get(index);
            activeTickets = ticketRepository.countActiveTicketsByAgentId(chosenAgent.getId());
        } else {
            // LEAST_BUSY: Rank by active workload count ascending
            chosenAgent = candidateAgents.stream()
                    .min(Comparator.comparingLong(agent -> ticketRepository.countActiveTicketsByAgentId(agent.getId())))
                    .orElse(candidateAgents.get(0));
            activeTickets = ticketRepository.countActiveTicketsByAgentId(chosenAgent.getId());
        }

        Long teamId = ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null;
        String teamName = ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : "Unassigned Team";

        String reason = String.format("Auto-assigned via %s algorithm (agent currently handling %d active tickets)",
                strategy, activeTickets);

        ticketService.assignTicket(ticketId, new AssignTicketRequest(chosenAgent.getId(), teamId, reason));

        log.info("Auto-assigned ticket {} to agent {} ({}) using {}",
                ticket.getTicketNumber(), chosenAgent.getFullName(), chosenAgent.getEmail(), strategy);

        return new AutoAssignResultDTO(
                ticket.getId(),
                ticket.getTicketNumber(),
                chosenAgent.getId(),
                chosenAgent.getFullName(),
                chosenAgent.getEmail(),
                teamId,
                teamName,
                strategy,
                activeTickets + 1, // Include newly assigned ticket
                reason
        );
    }

    /**
     * Balances and dispatches all unassigned active tickets across available staff.
     */
    @Transactional
    public AutoAssignBatchResultDTO autoAssignAllUnassigned(String rawStrategy) {
        String strategy = (rawStrategy != null && rawStrategy.equalsIgnoreCase("ROUND_ROBIN"))
                ? "ROUND_ROBIN" : "LEAST_BUSY";

        List<Ticket> unassignedTickets = ticketRepository.findUnassignedActiveTickets(PageRequest.of(0, 100)).getContent();
        List<AutoAssignResultDTO> results = new ArrayList<>();

        for (Ticket ticket : unassignedTickets) {
            try {
                AutoAssignResultDTO assigned = autoAssignTicket(ticket.getId(), strategy);
                results.add(assigned);
            } catch (Exception e) {
                log.warn("Could not auto-assign ticket {}: {}", ticket.getTicketNumber(), e.getMessage());
            }
        }

        return new AutoAssignBatchResultDTO(results.size(), strategy, results);
    }

    private List<User> getCandidateAgents(Ticket ticket) {
        if (ticket.getAssignedTeam() != null) {
            List<TeamMember> members = teamMemberRepository.findByTeamIdWithUsers(ticket.getAssignedTeam().getId());
            List<User> teamAgents = members.stream()
                    .map(TeamMember::getUser)
                    .filter(u -> u.isActive() && (u.getRole() == Role.SUPPORT_AGENT || u.getRole() == Role.SUPPORT_MANAGER || u.getRole() == Role.ADMIN))
                    .collect(Collectors.toList());

            if (!teamAgents.isEmpty()) {
                return teamAgents;
            }
        }

        // Fallback to all active support agents and managers
        return userRepository.findByRoleIn(List.of(Role.SUPPORT_AGENT, Role.SUPPORT_MANAGER)).stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
}
