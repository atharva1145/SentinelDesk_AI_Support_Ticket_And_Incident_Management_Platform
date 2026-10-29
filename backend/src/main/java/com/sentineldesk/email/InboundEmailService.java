package com.sentineldesk.email;

import com.sentineldesk.ai.AiTriageResult;
import com.sentineldesk.ai.AiTriageService;
import com.sentineldesk.comment.CommentRequest;
import com.sentineldesk.comment.CommentResponseDTO;
import com.sentineldesk.comment.CommentService;
import com.sentineldesk.history.TicketStatusHistory;
import com.sentineldesk.history.TicketStatusHistoryRepository;
import com.sentineldesk.notification.SseNotificationService;
import com.sentineldesk.security.UserPrincipal;
import com.sentineldesk.sla.SlaService;
import com.sentineldesk.ticket.*;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InboundEmailService {

    private static final Logger log = LoggerFactory.getLogger(InboundEmailService.class);
    private static final Pattern TICKET_NUM_PATTERN = Pattern.compile("SD-\\d{4}-\\d{6}", Pattern.CASE_INSENSITIVE);

    private final TicketRepository ticketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final UserRepository userRepository;
    private final SlaService slaService;
    private final AiTriageService aiTriageService;
    private final CommentService commentService;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final SseNotificationService sseNotificationService;
    private final PasswordEncoder passwordEncoder;

    public InboundEmailService(TicketRepository ticketRepository,
                               TicketNumberGenerator ticketNumberGenerator,
                               UserRepository userRepository,
                               SlaService slaService,
                               AiTriageService aiTriageService,
                               CommentService commentService,
                               TicketStatusHistoryRepository statusHistoryRepository,
                               SseNotificationService sseNotificationService,
                               PasswordEncoder passwordEncoder) {
        this.ticketRepository = ticketRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.userRepository = userRepository;
        this.slaService = slaService;
        this.aiTriageService = aiTriageService;
        this.commentService = commentService;
        this.statusHistoryRepository = statusHistoryRepository;
        this.sseNotificationService = sseNotificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InboundEmailResponseDTO processInboundEmail(InboundEmailRequest request) {
        String from = request.getFrom().trim().toLowerCase();
        String subject = request.getSubject().trim();
        String body = request.getBody().trim();

        log.info("Processing inbound email from {} with subject: {}", from, subject);

        // Find or provision requester user
        User sender = findOrCreateUser(from, request.getSenderName());

        // Authenticate context as sender for comment/ticket creation permissions
        UserPrincipal principal = UserPrincipal.create(sender);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Check if subject references an existing ticket (e.g. Re: [SD-2026-000001] ...)
        Matcher matcher = TICKET_NUM_PATTERN.matcher(subject);
        if (matcher.find()) {
            String ticketNumber = matcher.group().toUpperCase();
            Optional<Ticket> existingTicketOpt = ticketRepository.findByTicketNumber(ticketNumber);

            if (existingTicketOpt.isPresent()) {
                Ticket ticket = existingTicketOpt.get();
                log.info("Inbound email references existing ticket #{}. Appending public comment.", ticketNumber);

                CommentRequest commentReq = new CommentRequest("[Inbound Email Update]\n" + body, false);

                CommentResponseDTO commentDTO = commentService.addComment(ticket.getId(), commentReq);

                return new InboundEmailResponseDTO(
                        "COMMENT_ADDED",
                        ticket.getTicketNumber(),
                        ticket.getId(),
                        "Email reply added to ticket #" + ticket.getTicketNumber(),
                        from
                );
            }
        }

        // New Ticket Creation via Email
        log.info("No matching ticket found in subject. Generating new ticket from inbound email.");

        String ticketNumber = ticketNumberGenerator.nextTicketNumber();
        Ticket ticket = new Ticket(
                ticketNumber,
                subject,
                body,
                sender,
                TicketCategory.SOFTWARE,
                TicketPriority.P3
        );
        ticket.setSource(TicketSource.EMAIL);
        ticket.setStatus(TicketStatus.NEW);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Initialize SLA
        slaService.initTrackerForTicket(savedTicket);

        // Automated AI Triage
        aiTriageService.generateTriage(savedTicket);

        // Record initial status history
        TicketStatusHistory statusHistory = new TicketStatusHistory(
                savedTicket, sender, null, TicketStatus.NEW, "Ticket created via Inbound Email from " + from
        );
        statusHistoryRepository.save(statusHistory);

        // Broadcast real-time SSE alert
        try {
            sseNotificationService.broadcast("TICKET_CREATED", Map.of(
                    "ticketNumber", savedTicket.getTicketNumber(),
                    "title", savedTicket.getTitle(),
                    "priority", savedTicket.getPriority().name(),
                    "category", savedTicket.getCategory().name(),
                    "source", "EMAIL"
            ));
        } catch (Exception ignored) {}

        return new InboundEmailResponseDTO(
                "TICKET_CREATED",
                savedTicket.getTicketNumber(),
                savedTicket.getId(),
                "Support ticket #" + savedTicket.getTicketNumber() + " successfully created from inbound email.",
                from
        );
    }

    private User findOrCreateUser(String email, String senderName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String name = (senderName != null && !senderName.trim().isEmpty())
                    ? senderName.trim()
                    : email.split("@")[0];
            String randomHash = passwordEncoder.encode("Password123!");
            User newUser = new User(email, randomHash, name, Role.REQUESTER, "External Inbound");
            newUser.setActive(true);
            return userRepository.save(newUser);
        });
    }
}
