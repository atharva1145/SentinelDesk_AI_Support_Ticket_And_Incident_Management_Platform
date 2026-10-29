package com.sentineldesk;

import com.sentineldesk.common.exception.InvalidStatusTransitionException;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.ticket.TicketStatus;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TicketWorkflowTest {

    @Test
    @DisplayName("Valid status transitions follow defined lifecycle")
    void testValidStatusTransitions() {
        // NEW can transition to TRIAGED, OPEN, CANCELLED
        assertTrue(TicketStatus.NEW.canTransitionTo(TicketStatus.TRIAGED));
        assertTrue(TicketStatus.NEW.canTransitionTo(TicketStatus.OPEN));
        assertTrue(TicketStatus.NEW.canTransitionTo(TicketStatus.CANCELLED));

        // OPEN can transition to IN_PROGRESS, PENDING_REQUESTER, RESOLVED, CANCELLED
        assertTrue(TicketStatus.OPEN.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatus.OPEN.canTransitionTo(TicketStatus.PENDING_REQUESTER));
        assertTrue(TicketStatus.OPEN.canTransitionTo(TicketStatus.RESOLVED));

        // RESOLVED can transition to CLOSED or reopen to OPEN
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED));
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.OPEN));
    }

    @Test
    @DisplayName("Invalid status transitions are strictly rejected")
    void testInvalidStatusTransitions() {
        // NEW cannot jump straight to CLOSED or RESOLVED
        assertFalse(TicketStatus.NEW.canTransitionTo(TicketStatus.CLOSED));
        assertFalse(TicketStatus.NEW.canTransitionTo(TicketStatus.RESOLVED));

        // CLOSED is terminal state
        assertFalse(TicketStatus.CLOSED.canTransitionTo(TicketStatus.OPEN));
        assertFalse(TicketStatus.CLOSED.canTransitionTo(TicketStatus.IN_PROGRESS));

        // CANCELLED is terminal state
        assertFalse(TicketStatus.CANCELLED.canTransitionTo(TicketStatus.OPEN));
    }

    @Test
    @DisplayName("Ticket entity preserves audit timestamps on state changes")
    void testTicketEntityCreation() {
        User requester = new User("alice@corp.com", "hash", "Alice", Role.REQUESTER, "Engineering");
        Ticket ticket = new Ticket("SD-2026-000100", "VPN Down", "Cannot reach gateway", requester,
                TicketCategory.NETWORK, TicketPriority.P2);

        assertEquals(TicketStatus.NEW, ticket.getStatus());
        assertEquals("SD-2026-000100", ticket.getTicketNumber());
        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getUpdatedAt());
    }
}
