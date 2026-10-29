package com.sentineldesk.escalation;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.notification.NotificationService;
import com.sentineldesk.sla.SlaTracker;
import com.sentineldesk.sla.SlaTrackerRepository;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final EscalationRepository escalationRepository;
    private final SlaTrackerRepository slaTrackerRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${sentineldesk.sla.warning-threshold-percent:80}")
    private int warningThresholdPercent;

    public EscalationService(EscalationRepository escalationRepository,
                             SlaTrackerRepository slaTrackerRepository,
                             NotificationService notificationService,
                             UserRepository userRepository) {
        this.escalationRepository = escalationRepository;
        this.slaTrackerRepository = slaTrackerRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public int checkAndProcessEscalations() {
        LocalDateTime now = LocalDateTime.now();
        List<SlaTracker> activeTrackers = slaTrackerRepository.findActiveTrackersWithTickets();
        int triggeredCount = 0;

        for (SlaTracker tracker : activeTrackers) {
            Ticket ticket = tracker.getTicket();

            // 1. Check Resolution Breach
            if (now.isAfter(tracker.getResolutionDeadline()) && !Boolean.TRUE.equals(tracker.getResolutionMet())) {
                tracker.setResolutionBreached(true);
                if (!escalationRepository.existsByTicketIdAndEscalationLevel(ticket.getId(), EscalationLevel.RESOLUTION_BREACH)) {
                    triggerEscalation(
                            ticket,
                            tracker,
                            EscalationLevel.RESOLUTION_BREACH,
                            "Resolution SLA deadline of " + tracker.getResolutionDeadline() + " was breached."
                    );
                    triggeredCount++;
                }
            }

            // 2. Check First Response Breach
            if (now.isAfter(tracker.getFirstResponseDeadline()) && !Boolean.TRUE.equals(tracker.getFirstResponseMet())) {
                tracker.setFirstResponseBreached(true);
                if (!escalationRepository.existsByTicketIdAndEscalationLevel(ticket.getId(), EscalationLevel.FIRST_RESPONSE_BREACH)) {
                    triggerEscalation(
                            ticket,
                            tracker,
                            EscalationLevel.FIRST_RESPONSE_BREACH,
                            "First response SLA deadline of " + tracker.getFirstResponseDeadline() + " was breached."
                    );
                    triggeredCount++;
                }
            }

            // 3. Check Warning Threshold (e.g. 80% elapsed time)
            if (!Boolean.TRUE.equals(tracker.getResolutionBreached()) && !Boolean.TRUE.equals(tracker.getResolutionMet())) {
                Duration totalWindow = Duration.between(tracker.getCreatedAt(), tracker.getResolutionDeadline());
                Duration elapsed = Duration.between(tracker.getCreatedAt(), now);

                if (!totalWindow.isZero() && !totalWindow.isNegative()) {
                    long percentElapsed = (elapsed.toSeconds() * 100) / totalWindow.toSeconds();
                    if (percentElapsed >= warningThresholdPercent && !Boolean.TRUE.equals(tracker.getWarningIssued())) {
                        tracker.setWarningIssued(true);
                        if (!escalationRepository.existsByTicketIdAndEscalationLevel(ticket.getId(), EscalationLevel.WARNING_80)) {
                            triggerEscalation(
                                    ticket,
                                    tracker,
                                    EscalationLevel.WARNING_80,
                                    "Ticket has reached " + percentElapsed + "% of its SLA resolution window."
                            );
                            triggeredCount++;
                        }
                    }
                }
            }

            slaTrackerRepository.save(tracker);
        }

        return triggeredCount;
    }

    private void triggerEscalation(Ticket ticket, SlaTracker tracker, EscalationLevel level, String reason) {
        tracker.setIsEscalated(true);

        User manager = findEscalationManager(ticket);

        EscalationEvent event = new EscalationEvent(ticket, tracker, level, reason, manager);
        escalationRepository.save(event);

        log.warn("ESCALATION TRIGGERED: Ticket #{} - Level: {} - Reason: {}",
                ticket.getTicketNumber(), level, reason);

        if (manager != null) {
            notificationService.createNotification(
                    manager,
                    "Ticket Escalation: " + ticket.getTicketNumber() + " [" + level + "]",
                    reason,
                    "/tickets/" + ticket.getId()
            );
        }

        if (ticket.getAssignedAgent() != null && (manager == null || !manager.getId().equals(ticket.getAssignedAgent().getId()))) {
            notificationService.createNotification(
                    ticket.getAssignedAgent(),
                    "SLA Alert: " + ticket.getTicketNumber() + " [" + level + "]",
                    reason,
                    "/tickets/" + ticket.getId()
            );
        }
    }

    private User findEscalationManager(Ticket ticket) {
        List<User> managers = userRepository.findByRole(Role.SUPPORT_MANAGER);
        if (!managers.isEmpty()) {
            return managers.get(0);
        }
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        return admins.isEmpty() ? null : admins.get(0);
    }

    @Transactional(readOnly = true)
    public List<EscalationEventDTO> getAllEscalations() {
        return escalationRepository.findAllWithDetails().stream()
                .map(EscalationEventDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public EscalationEventDTO updateStatus(Long id, EscalationStatus newStatus) {
        EscalationEvent event = escalationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escalation event not found with id: " + id));

        event.setStatus(newStatus);
        if (newStatus == EscalationStatus.RESOLVED) {
            event.setResolvedAt(LocalDateTime.now());
        }
        event = escalationRepository.save(event);
        return new EscalationEventDTO(event);
    }
}
