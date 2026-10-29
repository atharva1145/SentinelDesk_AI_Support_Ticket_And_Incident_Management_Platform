package com.sentineldesk.comment;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.notification.NotificationService;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.sla.SlaService;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.ticket.TicketService;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public CommentService(CommentRepository commentRepository,
                          TicketRepository ticketRepository,
                          UserRepository userRepository,
                          TicketService ticketService,
                          SlaService slaService,
                          NotificationService notificationService,
                          AuditService auditService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public CommentResponseDTO addComment(Long ticketId, CommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        ticketService.validateTicketAccess(ticket);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        // CRITICAL ACCESS RULE: Requesters cannot post internal notes
        boolean isInternal = request.isInternal();
        if (author.getRole() == Role.REQUESTER) {
            if (isInternal) {
                throw new AccessDeniedException("Requesters are not permitted to add internal support notes.");
            }
            isInternal = false;
        }

        TicketComment comment = new TicketComment(ticket, author, request.getContent(), isInternal);
        comment = commentRepository.save(comment);

        // Record first response if agent makes a public response to ticket
        if (!isInternal && SecurityUtils.isStaff() && ticket.getFirstResponseAt() == null) {
            slaService.recordFirstResponse(ticket);
            ticketRepository.save(ticket);
        }

        // Notifications
        if (!isInternal) {
            if (SecurityUtils.isStaff() && ticket.getRequester() != null) {
                notificationService.createNotification(
                        ticket.getRequester(),
                        "New reply on ticket " + ticket.getTicketNumber(),
                        author.getFullName() + " replied to your ticket.",
                        "/tickets/" + ticket.getId()
                );
            } else if (SecurityUtils.isRequester() && ticket.getAssignedAgent() != null) {
                notificationService.createNotification(
                        ticket.getAssignedAgent(),
                        "Requester updated ticket " + ticket.getTicketNumber(),
                        author.getFullName() + " added a public comment.",
                        "/tickets/" + ticket.getId()
                );
            }
        }

        auditService.logSync(currentUserId, isInternal ? "INTERNAL_NOTE_ADDED" : "COMMENT_ADDED",
                "TICKET", ticket.getId().toString(), "Added " + (isInternal ? "internal note" : "public reply"), null);

        return new CommentResponseDTO(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsForTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        ticketService.validateTicketAccess(ticket);

        List<TicketComment> comments;
        // CRITICAL ACCEPTANCE TEST: Requesters MUST NEVER receive internal notes!
        if (SecurityUtils.isRequester()) {
            comments = commentRepository.findPublicByTicketIdWithAuthor(ticketId);
        } else {
            comments = commentRepository.findAllByTicketIdWithAuthor(ticketId);
        }

        return comments.stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }
}
