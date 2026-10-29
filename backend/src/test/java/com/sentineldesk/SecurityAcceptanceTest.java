package com.sentineldesk;

import com.sentineldesk.comment.CommentRepository;
import com.sentineldesk.comment.CommentRequest;
import com.sentineldesk.comment.CommentService;
import com.sentineldesk.comment.TicketComment;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.notification.NotificationService;
import com.sentineldesk.security.UserPrincipal;
import com.sentineldesk.sla.SlaService;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.ticket.TicketService;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecurityAcceptanceTest {

    private CommentRepository commentRepository;
    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private TicketService ticketService;
    private SlaService slaService;
    private NotificationService notificationService;
    private AuditService auditService;
    private CommentService commentService;

    private User requesterA;
    private User requesterB;
    private User agent;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        ticketService = mock(TicketService.class);
        slaService = mock(SlaService.class);
        notificationService = mock(NotificationService.class);
        auditService = mock(AuditService.class);

        commentService = new CommentService(commentRepository, ticketRepository, userRepository,
                ticketService, slaService, notificationService, auditService);

        requesterA = new User("alice@corp.com", "hash", "Alice Requester", Role.REQUESTER, "Sales");
        requesterA.setId(101L);

        requesterB = new User("bob@corp.com", "hash", "Bob Requester", Role.REQUESTER, "Marketing");
        requesterB.setId(102L);

        agent = new User("agent@sentineldesk.com", "hash", "Agent Smith", Role.SUPPORT_AGENT, "Support");
        agent.setId(201L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Requester cannot view internal support notes")
    void testRequesterCannotViewInternalNotes() {
        // Authenticate as Requester A
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UserPrincipal.create(requesterA), null, UserPrincipal.create(requesterA).getAuthorities())
        );

        Ticket ticket = new Ticket("SD-2026-000010", "Password reset", "Need reset", requesterA, TicketCategory.ACCESS, TicketPriority.P3);
        ticket.setId(10L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        TicketComment publicComment = new TicketComment(ticket, agent, "Hello, password reset email sent.", false);
        publicComment.setId(1L);

        // findPublicByTicketIdWithAuthor returns only public comments
        when(commentRepository.findPublicByTicketIdWithAuthor(10L)).thenReturn(List.of(publicComment));

        var comments = commentService.getCommentsForTicket(10L);

        assertEquals(1, comments.size());
        assertFalse(comments.get(0).isInternal());
        // Verify internal notes query was never called for a requester
        verify(commentRepository, never()).findAllByTicketIdWithAuthor(10L);
    }

    @Test
    @DisplayName("Requester is denied from posting internal notes")
    void testRequesterCannotPostInternalNotes() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UserPrincipal.create(requesterA), null, UserPrincipal.create(requesterA).getAuthorities())
        );

        Ticket ticket = new Ticket("SD-2026-000010", "Issue", "Problem", requesterA, TicketCategory.SOFTWARE, TicketPriority.P3);
        ticket.setId(10L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(101L)).thenReturn(Optional.of(requesterA));

        CommentRequest request = new CommentRequest("Secret support comment", true);

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(10L, request));
    }
}
