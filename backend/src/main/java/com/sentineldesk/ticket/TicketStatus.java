package com.sentineldesk.ticket;

import java.util.EnumSet;
import java.util.Set;

public enum TicketStatus {
    NEW,
    TRIAGED,
    OPEN,
    IN_PROGRESS,
    PENDING_REQUESTER,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean canTransitionTo(TicketStatus target) {
        if (this == target) return true;

        return switch (this) {
            case NEW -> EnumSet.of(TRIAGED, OPEN, CANCELLED).contains(target);
            case TRIAGED -> EnumSet.of(OPEN, IN_PROGRESS, CANCELLED).contains(target);
            case OPEN -> EnumSet.of(IN_PROGRESS, PENDING_REQUESTER, RESOLVED, CANCELLED).contains(target);
            case IN_PROGRESS -> EnumSet.of(OPEN, PENDING_REQUESTER, RESOLVED, CANCELLED).contains(target);
            case PENDING_REQUESTER -> EnumSet.of(IN_PROGRESS, OPEN, RESOLVED, CANCELLED).contains(target);
            case RESOLVED -> EnumSet.of(CLOSED, OPEN).contains(target); // Reopening allows back to OPEN
            case CLOSED -> false; // Final terminal state
            case CANCELLED -> false; // Terminal state
        };
    }
}
