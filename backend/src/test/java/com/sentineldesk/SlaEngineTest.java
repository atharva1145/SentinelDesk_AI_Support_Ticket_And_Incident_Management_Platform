package com.sentineldesk;

import com.sentineldesk.escalation.EscalationLevel;
import com.sentineldesk.escalation.EscalationRepository;
import com.sentineldesk.escalation.EscalationService;
import com.sentineldesk.notification.NotificationService;
import com.sentineldesk.notification.SseNotificationService;
import com.sentineldesk.sla.*;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SlaEngineTest {

    private SlaPolicyRepository policyRepository;
    private SlaTrackerRepository trackerRepository;
    private EscalationRepository escalationRepository;
    private NotificationService notificationService;
    private SseNotificationService sseNotificationService;
    private UserRepository userRepository;
    private SlaService slaService;
    private EscalationService escalationService;

    @BeforeEach
    void setUp() {
        policyRepository = mock(SlaPolicyRepository.class);
        trackerRepository = mock(SlaTrackerRepository.class);
        escalationRepository = mock(EscalationRepository.class);
        notificationService = mock(NotificationService.class);
        sseNotificationService = mock(SseNotificationService.class);
        userRepository = mock(UserRepository.class);

        slaService = new SlaService(policyRepository, trackerRepository);
        escalationService = new EscalationService(escalationRepository, trackerRepository, notificationService, sseNotificationService, userRepository);
    }

    @Test
    @DisplayName("SLA Engine calculates accurate deadlines according to priority policy")
    void testSlaDeadlineCalculation() {
        SlaPolicy p1Policy = new SlaPolicy("P1 Critical", TicketPriority.P1, BigDecimal.valueOf(0.25), BigDecimal.valueOf(4.0), "4h SLA");
        when(policyRepository.findByPriority(TicketPriority.P1)).thenReturn(Optional.of(p1Policy));
        when(trackerRepository.save(any(SlaTracker.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = new User("test@corp.com", "hash", "Tester", Role.REQUESTER, "IT");
        Ticket ticket = new Ticket("SD-2026-000001", "Outage", "System down", user, TicketCategory.OUTAGE, TicketPriority.P1);
        ticket.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        SlaTracker tracker = slaService.initTrackerForTicket(ticket);

        assertNotNull(tracker);
        // First response 15 min after creation
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 15), tracker.getFirstResponseDeadline());
        // Resolution 4h after creation
        assertEquals(LocalDateTime.of(2026, 1, 1, 14, 0), tracker.getResolutionDeadline());
        assertEquals(tracker.getResolutionDeadline(), ticket.getSlaDeadline());
    }

    @Test
    @DisplayName("Overdue ticket generates escalation and repeated scheduler runs do NOT create duplicate escalations")
    void testDuplicateEscalationPrevention() {
        User user = new User("test@corp.com", "hash", "Tester", Role.REQUESTER, "IT");
        User manager = new User("mgr@corp.com", "hash", "Manager", Role.SUPPORT_MANAGER, "Ops");
        when(userRepository.findByRole(Role.SUPPORT_MANAGER)).thenReturn(List.of(manager));

        Ticket ticket = new Ticket("SD-2026-000002", "Bug", "Error in app", user, TicketCategory.SOFTWARE, TicketPriority.P2);
        ticket.setId(10L);
        ticket.setCreatedAt(LocalDateTime.now().minusHours(24));

        SlaPolicy policy = new SlaPolicy("P2", TicketPriority.P2, BigDecimal.valueOf(1.0), BigDecimal.valueOf(8.0), "8h");
        SlaTracker tracker = new SlaTracker(ticket, policy, ticket.getCreatedAt().plusHours(1), ticket.getCreatedAt().plusHours(8));
        tracker.setId(20L);

        // Mark first response as already met so only resolution SLA is evaluated
        tracker.setFirstResponseMet(true);

        when(trackerRepository.findActiveTrackersWithTickets()).thenReturn(List.of(tracker));
        when(escalationRepository.existsByTicketIdAndEscalationLevel(10L, EscalationLevel.RESOLUTION_BREACH))
                .thenReturn(false) // First run: does not exist
                .thenReturn(true); // Second run: already exists

        // Run 1: triggers resolution breach escalation
        int firstRun = escalationService.checkAndProcessEscalations();
        assertEquals(1, firstRun, "First run should trigger exactly 1 resolution breach escalation");
        verify(escalationRepository, times(1)).save(any());

        // Run 2: repeated run MUST NOT create duplicate escalation
        int secondRun = escalationService.checkAndProcessEscalations();
        assertEquals(0, secondRun, "Repeated execution must not create duplicate escalations");
        verify(escalationRepository, times(1)).save(any()); // Still only 1 total save
    }

    @Test
    @DisplayName("ITIL SLA pause pauses clock and pushes deadline forward upon resume while suppressing escalations")
    void testSlaPauseResumeEngine() {
        User user = new User("test@corp.com", "hash", "Tester", Role.REQUESTER, "IT");
        Ticket ticket = new Ticket("SD-2026-000003", "Hardware", "Screen flicker", user, TicketCategory.HARDWARE, TicketPriority.P3);
        ticket.setId(30L);
        ticket.setCreatedAt(LocalDateTime.of(2026, 3, 1, 9, 0));

        LocalDateTime originalDeadline = LocalDateTime.of(2026, 3, 1, 17, 0);
        SlaPolicy policy = new SlaPolicy("P3", TicketPriority.P3, BigDecimal.valueOf(4.0), BigDecimal.valueOf(8.0), "8h");
        SlaTracker tracker = new SlaTracker(ticket, policy, ticket.getCreatedAt().plusHours(4), originalDeadline);
        tracker.setId(31L);

        when(trackerRepository.findByTicketId(30L)).thenReturn(Optional.of(tracker));
        when(trackerRepository.save(any(SlaTracker.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Pause SLA when awaiting requester response
        LocalDateTime pauseTime = LocalDateTime.of(2026, 3, 1, 11, 0);
        tracker.pause(pauseTime);
        assertTrue(tracker.getIsPaused());
        assertEquals(pauseTime, tracker.getPausedAt());

        // 2. Escalation engine must SUPPRESS breach checks while paused
        when(trackerRepository.findActiveTrackersWithTickets()).thenReturn(List.of(tracker));
        int escalationsWhilePaused = escalationService.checkAndProcessEscalations();
        assertEquals(0, escalationsWhilePaused, "Paused tickets must not trigger breaches or escalations");

        // 3. Resume SLA 120 minutes later
        tracker.resume(pauseTime.plusMinutes(120));
        assertFalse(tracker.getIsPaused());
        assertNull(tracker.getPausedAt());
        assertEquals(120, tracker.getTotalPausedMinutes());

        // 4. Verify resolution deadline was pushed forward by exactly 120 minutes
        assertEquals(originalDeadline.plusMinutes(120), tracker.getResolutionDeadline());
        assertEquals(LocalDateTime.of(2026, 3, 1, 19, 0), tracker.getResolutionDeadline());
        assertEquals(LocalDateTime.of(2026, 3, 1, 19, 0), ticket.getSlaDeadline());
    }
}
