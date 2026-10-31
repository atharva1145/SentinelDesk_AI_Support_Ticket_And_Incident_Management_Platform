package com.sentineldesk;

import com.sentineldesk.routing.AutoAssignResultDTO;
import com.sentineldesk.routing.RoutingService;
import com.sentineldesk.team.TeamMemberRepository;
import com.sentineldesk.ticket.*;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RoutingServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private TeamMemberRepository teamMemberRepository;
    private TicketService ticketService;
    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        teamMemberRepository = mock(TeamMemberRepository.class);
        ticketService = mock(TicketService.class);

        routingService = new RoutingService(ticketRepository, userRepository, teamMemberRepository, ticketService);
    }

    @Test
    @DisplayName("Least-Busy routing algorithm selects agent with lowest active ticket workload")
    void testLeastBusyRouting() {
        User requester = new User("user@test.com", "hash", "Requester", Role.REQUESTER, "IT");
        Ticket ticket = new Ticket("SD-2026-000099", "VPN issue", "Cannot connect", requester, TicketCategory.NETWORK, TicketPriority.P2);
        ticket.setId(99L);

        User busyAgent = new User("busy@sentineldesk.com", "hash", "Busy Agent", Role.SUPPORT_AGENT, "IT");
        busyAgent.setId(101L);
        busyAgent.setActive(true);

        User freeAgent = new User("free@sentineldesk.com", "hash", "Free Agent", Role.SUPPORT_AGENT, "IT");
        freeAgent.setId(102L);
        freeAgent.setActive(true);

        when(ticketRepository.findById(99L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByRoleIn(any())).thenReturn(List.of(busyAgent, freeAgent));

        // busyAgent has 5 open tickets, freeAgent has 1 open ticket
        when(ticketRepository.countActiveTicketsByAgentId(101L)).thenReturn(5L);
        when(ticketRepository.countActiveTicketsByAgentId(102L)).thenReturn(1L);

        AutoAssignResultDTO result = routingService.autoAssignTicket(99L, "LEAST_BUSY");

        assertNotNull(result);
        assertEquals(102L, result.getAssignedAgentId());
        assertEquals("Free Agent", result.getAssignedAgentName());
        assertEquals("LEAST_BUSY", result.getStrategy());
        verify(ticketService, times(1)).assignTicket(eq(99L), any(AssignTicketRequest.class));
    }

    @Test
    @DisplayName("Round-Robin routing distributes work evenly among candidate staff")
    void testRoundRobinRouting() {
        User requester = new User("user@test.com", "hash", "Requester", Role.REQUESTER, "IT");
        Ticket ticket1 = new Ticket("SD-2026-000001", "Issue 1", "Desc 1", requester, TicketCategory.SOFTWARE, TicketPriority.P3);
        ticket1.setId(1L);
        Ticket ticket2 = new Ticket("SD-2026-000002", "Issue 2", "Desc 2", requester, TicketCategory.SOFTWARE, TicketPriority.P3);
        ticket2.setId(2L);

        User agentA = new User("a@sentineldesk.com", "hash", "Agent A", Role.SUPPORT_AGENT, "IT");
        agentA.setId(201L);
        agentA.setActive(true);

        User agentB = new User("b@sentineldesk.com", "hash", "Agent B", Role.SUPPORT_AGENT, "IT");
        agentB.setId(202L);
        agentB.setActive(true);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
        when(userRepository.findByRoleIn(any())).thenReturn(List.of(agentA, agentB));

        AutoAssignResultDTO res1 = routingService.autoAssignTicket(1L, "ROUND_ROBIN");
        AutoAssignResultDTO res2 = routingService.autoAssignTicket(2L, "ROUND_ROBIN");

        assertNotNull(res1);
        assertNotNull(res2);
        assertNotEquals(res1.getAssignedAgentId(), res2.getAssignedAgentId(), "Consecutive assignments should alternate in round-robin");
    }
}
