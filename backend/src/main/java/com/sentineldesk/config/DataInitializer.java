package com.sentineldesk.config;

import com.sentineldesk.ai.AiTriageService;
import com.sentineldesk.comment.CommentRequest;
import com.sentineldesk.comment.CommentService;
import com.sentineldesk.incident.CreateIncidentRequest;
import com.sentineldesk.incident.IncidentDTO;
import com.sentineldesk.incident.IncidentService;
import com.sentineldesk.incident.IncidentSeverity;
import com.sentineldesk.security.UserPrincipal;
import com.sentineldesk.ticket.*;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final CommentService commentService;
    private final IncidentService incidentService;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TicketRepository ticketRepository,
                           TicketService ticketService,
                           CommentService commentService,
                           IncidentService incidentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.incidentService = incidentService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking demo user credentials and initial seed data...");

        // Ensure BCrypt password for demo users
        String defaultPasswordHash = passwordEncoder.encode("Password123!");

        ensureUser("admin@sentineldesk.com", defaultPasswordHash, "System Administrator", Role.ADMIN, "IT Infrastructure");
        ensureUser("manager@sentineldesk.com", defaultPasswordHash, "Sarah Connor", Role.SUPPORT_MANAGER, "IT Service Management");
        ensureUser("agent1@sentineldesk.com", defaultPasswordHash, "Alex Rivera", Role.SUPPORT_AGENT, "Tier 1 Service Desk");
        ensureUser("agent2@sentineldesk.com", defaultPasswordHash, "Elena Rostova", Role.SUPPORT_AGENT, "Network Operations");
        ensureUser("user@sentineldesk.com", defaultPasswordHash, "David Miller", Role.REQUESTER, "Sales & Marketing");
        ensureUser("user2@sentineldesk.com", defaultPasswordHash, "Rachel Green", Role.REQUESTER, "Finance");

        // If no tickets exist, create rich demo tickets & an incident
        if (ticketRepository.count() == 0) {
            log.info("Seeding initial tickets, comments, and incidents...");
            seedInitialDemoData();
        }

        log.info("SentinelDesk AI data initialization completed successfully.");
    }

    private void ensureUser(String email, String passwordHash, String fullName, Role role, String department) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            user.setPasswordHash(passwordHash);
            user.setFullName(fullName);
            user.setRole(role);
            user.setDepartment(department);
            user.setActive(true);
            userRepository.save(user);
        }, () -> {
            User newUser = new User(email, passwordHash, fullName, role, department);
            userRepository.save(newUser);
        });
    }

    private void seedInitialDemoData() {
        User requester = userRepository.findByEmail("user@sentineldesk.com").orElse(null);
        User agent1 = userRepository.findByEmail("agent1@sentineldesk.com").orElse(null);
        User manager = userRepository.findByEmail("manager@sentineldesk.com").orElse(null);

        if (requester == null || agent1 == null) return;

        // Set security context to requester for ticket creation
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UserPrincipal.create(requester), null, UserPrincipal.create(requester).getAuthorities())
        );

        // Ticket 1: Outage (P1)
        CreateTicketRequest t1 = new CreateTicketRequest(
                "Primary PostgreSQL database connection pool exhausted",
                "Application servers in us-east-1 are throwing connection timeout exceptions. Customer checkout is currently failing for all users.",
                TicketCategory.OUTAGE,
                TicketPriority.P1
        );
        TicketResponseDTO ticket1 = ticketService.createTicket(t1);

        // Ticket 2: Network Issue (P2)
        CreateTicketRequest t2 = new CreateTicketRequest(
                "VPN Gateway dropping connections in London branch",
                "Multiple remote staff report intermittent disconnection every 10 minutes when connected to Gateway-EU2.",
                TicketCategory.NETWORK,
                TicketPriority.P2
        );
        TicketResponseDTO ticket2 = ticketService.createTicket(t2);

        // Ticket 3: Access Request (P3)
        CreateTicketRequest t3 = new CreateTicketRequest(
                "Need access to Financial Reporting Tableau dashboard",
                "Recently joined sales finance team and require viewer permissions on the Q4 revenue projection workbook.",
                TicketCategory.ACCESS,
                TicketPriority.P3
        );
        TicketResponseDTO ticket3 = ticketService.createTicket(t3);

        // Assign ticket 1 to Agent 1
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UserPrincipal.create(manager), null, UserPrincipal.create(manager).getAuthorities())
        );
        ticketService.assignTicket(ticket1.getId(), new AssignTicketRequest(agent1.getId(), 1L, "Critical outage triage"));
        ticketService.changeStatus(ticket1.getId(), new StatusChangeRequest(TicketStatus.IN_PROGRESS, "Diagnosing DB connection leaks"));

        // Add internal note and public reply
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UserPrincipal.create(agent1), null, UserPrincipal.create(agent1).getAuthorities())
        );
        commentService.addComment(ticket1.getId(), new CommentRequest(
                "Investigating pg_stat_activity. 35 long-running report queries are holding open idle in transaction connections.",
                true // internal note
        ));
        commentService.addComment(ticket1.getId(), new CommentRequest(
                "We have identified the root cause and are terminating lingering reporting queries. Connectivity should restore in 5 minutes.",
                false // public reply
        ));

        // Create Incident grouping ticket 1
        CreateIncidentRequest incReq = new CreateIncidentRequest();
        incReq.setTitle("Production Database Outage - US-East-1");
        incReq.setDescription("Major database connection pool exhaustion causing checkout service degradation.");
        incReq.setSeverity(IncidentSeverity.CRITICAL);
        incReq.setAffectedService("Checkout & Payment Gateway");
        incReq.setOwnerId(manager.getId());
        incReq.setTicketIds(List.of(ticket1.getId()));
        incidentService.createIncident(incReq);

        SecurityContextHolder.clearContext();
    }
}
