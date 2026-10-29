package com.sentineldesk.ticket;

import com.sentineldesk.ai.AiTriageService;
import com.sentineldesk.common.exception.BadRequestException;
import com.sentineldesk.common.exception.InvalidStatusTransitionException;
import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.common.response.PagedResponse;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.history.TicketAssignmentHistory;
import com.sentineldesk.history.TicketAssignmentHistoryRepository;
import com.sentineldesk.history.TicketStatusHistory;
import com.sentineldesk.history.TicketStatusHistoryRepository;
import com.sentineldesk.notification.NotificationService;
import com.sentineldesk.notification.SseNotificationService;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.sla.SlaService;
import com.sentineldesk.team.Team;
import com.sentineldesk.team.TeamRepository;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SlaService slaService;
    private final AiTriageService aiTriageService;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository,
                         TicketNumberGenerator ticketNumberGenerator,
                         UserRepository userRepository,
                         TeamRepository teamRepository,
                         SlaService slaService,
                         AiTriageService aiTriageService,
                         TicketStatusHistoryRepository statusHistoryRepository,
                         TicketAssignmentHistoryRepository assignmentHistoryRepository,
                         NotificationService notificationService,
                         SseNotificationService sseNotificationService,
                         AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.slaService = slaService;
        this.aiTriageService = aiTriageService;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.notificationService = notificationService;
        this.sseNotificationService = sseNotificationService;
        this.auditService = auditService;
    }

    @Transactional
    public TicketResponseDTO createTicket(CreateTicketRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User requester = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester user not found: " + currentUserId));

        String ticketNumber = ticketNumberGenerator.nextTicketNumber();
        Ticket ticket = new Ticket(ticketNumber, request.getTitle(), request.getDescription(),
                requester, request.getCategory(), request.getPriority());

        if (request.getAssignedTeamId() != null) {
            teamRepository.findById(request.getAssignedTeamId()).ifPresent(ticket::setAssignedTeam);
        }

        ticket = ticketRepository.save(ticket);

        // 1. Initialize SLA tracker and calculate deadlines
        slaService.initTrackerForTicket(ticket);

        // 2. Safely run AI triage
        aiTriageService.generateTriage(ticket);

        // 3. Record status history
        TicketStatusHistory statusHistory = new TicketStatusHistory(
                ticket, requester, null, TicketStatus.NEW, "Ticket created by " + requester.getFullName()
        );
        statusHistoryRepository.save(statusHistory);

        // 4. Audit
        auditService.logSync(currentUserId, "TICKET_CREATED", "TICKET", ticket.getId().toString(),
                "Created ticket " + ticket.getTicketNumber() + " with priority " + ticket.getPriority(), null);

        // 5. Broadcast real-time SSE event
        try {
            sseNotificationService.broadcast("TICKET_CREATED", java.util.Map.of(
                    "ticketNumber", ticket.getTicketNumber(),
                    "title", ticket.getTitle(),
                    "priority", ticket.getPriority().name(),
                    "category", ticket.getCategory().name()
            ));
        } catch (Exception ignored) {}

        return getTicketDetails(ticket.getId());
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketDetails(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        validateTicketAccess(ticket);

        TicketResponseDTO dto = new TicketResponseDTO(ticket);
        dto.setSlaTracker(slaService.getTrackerForTicket(ticket.getId()));
        dto.setAiSuggestion(aiTriageService.getLatestSuggestionForTicket(ticket.getId()));
        return dto;
    }

    public void validateTicketAccess(Ticket ticket) {
        if (SecurityUtils.isRequester()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (!ticket.getRequester().getId().equals(currentUserId)) {
                log.warn("Requester {} unauthorized attempt to access ticket {}", currentUserId, ticket.getTicketNumber());
                throw new AccessDeniedException("Access denied: You can only view your own support tickets.");
            }
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketResponseDTO> searchTickets(
            String query,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long agentId,
            Long teamId,
            Boolean unassigned,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Ticket> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // If user is REQUESTER, force filter by requester ID
            if (SecurityUtils.isRequester()) {
                predicates.add(cb.equal(root.get("requester").get("id"), SecurityUtils.getCurrentUserId()));
            }

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ticketNumber")), pattern),
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (agentId != null) {
                predicates.add(cb.equal(root.get("assignedAgent").get("id"), agentId));
            }

            if (teamId != null) {
                predicates.add(cb.equal(root.get("assignedTeam").get("id"), teamId));
            }

            if (Boolean.TRUE.equals(unassigned)) {
                predicates.add(cb.isNull(root.get("assignedAgent")));
                predicates.add(root.get("status").in(List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.CANCELLED)).not());
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

        List<TicketResponseDTO> dtoList = ticketPage.getContent().stream()
                .map(TicketResponseDTO::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtoList,
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages(),
                ticketPage.isLast()
        );
    }

    @Transactional
    public TicketResponseDTO updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        validateTicketAccess(ticket);

        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getCategory() != null) ticket.setCategory(request.getCategory());

        ticket = ticketRepository.save(ticket);
        return getTicketDetails(ticket.getId());
    }

    @Transactional
    public TicketResponseDTO changeStatus(Long ticketId, StatusChangeRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        validateTicketAccess(ticket);

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return getTicketDetails(ticketId);
        }

        // Enforce valid status transitions
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(oldStatus.name(), newStatus.name());
        }

        ticket.setStatus(newStatus);

        // ITIL SLA Clock Pause / Resume handling
        if (newStatus == TicketStatus.PENDING_REQUESTER) {
            slaService.pauseSla(ticket);
        } else if (oldStatus == TicketStatus.PENDING_REQUESTER) {
            slaService.resumeSla(ticket);
        }

        // Handle ticket reopening
        if (oldStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.OPEN) {
            ticket.setReopenedCount(ticket.getReopenedCount() + 1);
            ticket.setResolvedAt(null);
            ticket.setResolutionSummary(null);
        }

        // Record first response timestamp if transitioning out of NEW / TRIAGED to OPEN / IN_PROGRESS
        if ((newStatus == TicketStatus.OPEN || newStatus == TicketStatus.IN_PROGRESS) && ticket.getFirstResponseAt() == null) {
            slaService.recordFirstResponse(ticket);
        }

        // Record resolution
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolutionSummary(request.getResolutionSummary());
            slaService.recordResolution(ticket);
        }

        // Closed timestamp
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket = ticketRepository.save(ticket);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        TicketStatusHistory history = new TicketStatusHistory(ticket, currentUser, oldStatus, newStatus, request.getReason());
        statusHistoryRepository.save(history);

        auditService.logSync(currentUserId, "STATUS_CHANGE", "TICKET", ticket.getId().toString(),
                "Changed status from " + oldStatus + " to " + newStatus + ". Reason: " + request.getReason(), null);

        // Notify requester on status changes
        if (ticket.getRequester() != null && (currentUser == null || !currentUser.getId().equals(ticket.getRequester().getId()))) {
            notificationService.createNotification(
                    ticket.getRequester(),
                    "Ticket " + ticket.getTicketNumber() + " status updated",
                    "Your ticket status has been changed to " + newStatus,
                    "/tickets/" + ticket.getId()
            );
        }

        return getTicketDetails(ticket.getId());
    }

    @Transactional
    public TicketResponseDTO assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        User oldAgent = ticket.getAssignedAgent();
        Team oldTeam = ticket.getAssignedTeam();

        User newAgent = request.getAgentId() != null ?
                userRepository.findById(request.getAgentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + request.getAgentId()))
                : null;

        Team newTeam = request.getTeamId() != null ?
                teamRepository.findById(request.getTeamId())
                        .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + request.getTeamId()))
                : oldTeam;

        ticket.setAssignedAgent(newAgent);
        ticket.setAssignedTeam(newTeam);

        // If ticket was NEW, advance to TRIAGED or OPEN
        if (ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        ticket = ticketRepository.save(ticket);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        TicketAssignmentHistory history = new TicketAssignmentHistory(
                ticket, currentUser, oldAgent, newAgent, oldTeam, newTeam, request.getReason()
        );
        assignmentHistoryRepository.save(history);

        auditService.logSync(currentUserId, "ASSIGNMENT_CHANGE", "TICKET", ticket.getId().toString(),
                "Assigned to agent " + (newAgent != null ? newAgent.getFullName() : "None") +
                ", team " + (newTeam != null ? newTeam.getName() : "None"), null);

        if (newAgent != null) {
            notificationService.createNotification(
                    newAgent,
                    "Ticket Assigned: " + ticket.getTicketNumber(),
                    "You have been assigned ticket: " + ticket.getTitle(),
                    "/tickets/" + ticket.getId()
            );
        }

        return getTicketDetails(ticket.getId());
    }

    @Transactional
    public TicketResponseDTO changePriority(Long ticketId, PriorityChangeRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        TicketPriority oldPriority = ticket.getPriority();
        TicketPriority newPriority = request.getPriority();

        ticket.setPriority(newPriority);
        slaService.updateTicketPrioritySla(ticket, newPriority);
        ticket = ticketRepository.save(ticket);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditService.logSync(currentUserId, "PRIORITY_CHANGE", "TICKET", ticket.getId().toString(),
                "Changed priority from " + oldPriority + " to " + newPriority + ". Reason: " + request.getReason(), null);

        return getTicketDetails(ticket.getId());
    }

    @Transactional(readOnly = true)
    public List<TicketStatusHistory> getStatusHistory(Long ticketId) {
        return statusHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional(readOnly = true)
    public List<TicketAssignmentHistory> getAssignmentHistory(Long ticketId) {
        return assignmentHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public TicketResponseDTO submitCsat(Long ticketId, CsatRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        validateTicketAccess(ticket);

        ticket.setCsatRating(request.getRating());
        ticket.setCsatFeedback(request.getFeedback());

        // Once rated by requester, automatically close resolved ticket
        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());

            Long currentUserId = SecurityUtils.getCurrentUserId();
            User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
            TicketStatusHistory history = new TicketStatusHistory(ticket, currentUser, TicketStatus.RESOLVED, TicketStatus.CLOSED, "Closed automatically via Customer Satisfaction Rating");
            statusHistoryRepository.save(history);
        }

        ticket = ticketRepository.save(ticket);
        return getTicketDetails(ticket.getId());
    }
}
