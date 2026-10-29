package com.sentineldesk;

import com.sentineldesk.ai.*;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.team.TeamRepository;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AiTriageSafetyTest {

    private AiProviderClient aiProviderClient;
    private TicketAiSuggestionRepository suggestionRepository;
    private TicketRepository ticketRepository;
    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private AuditService auditService;
    private AiTriageService aiTriageService;

    @BeforeEach
    void setUp() {
        aiProviderClient = new AiProviderClient();
        suggestionRepository = mock(TicketAiSuggestionRepository.class);
        ticketRepository = mock(TicketRepository.class);
        teamRepository = mock(TeamRepository.class);
        userRepository = mock(UserRepository.class);
        auditService = mock(AuditService.class);

        aiTriageService = new AiTriageService(aiProviderClient, suggestionRepository, ticketRepository,
                teamRepository, userRepository, auditService);
    }

    @Test
    @DisplayName("Deterministic fallback triage executes successfully when external AI is disabled")
    void testFallbackTriage() {
        AiTriageResult result = aiProviderClient.generateDeterministicFallback(
                "VPN gateway down in London office",
                "Multiple users are disconnected from internal network"
        );

        assertNotNull(result);
        assertEquals(TicketCategory.NETWORK, result.getCategory());
        assertEquals(TicketPriority.P2, result.getSuggestedPriority());
        assertEquals("Network Operations (NOC)", result.getSuggestedTeam());
        assertFalse(result.getDiagnosticQuestions().isEmpty());
    }

    @Test
    @DisplayName("Malformed JSON output from LLM does not crash or corrupt ticket")
    void testMalformedJsonHandling() {
        String invalidJson = "{ malformed json: not valid ... ";
        AiTriageResult parsed = aiProviderClient.parseAndValidateJson(invalidJson);
        assertNull(parsed); // Returns null safely without throwing uncaught exceptions
    }

    @Test
    @DisplayName("Human override records new values, audit reason, and sets flags")
    void testHumanOverride() {
        User requester = new User("req@corp.com", "hash", "Req", Role.REQUESTER, "Sales");
        Ticket ticket = new Ticket("SD-2026-000003", "Bug in ERP", "Cannot generate invoice", requester,
                TicketCategory.SOFTWARE, TicketPriority.P3);
        ticket.setId(5L);

        TicketAiSuggestion suggestion = new TicketAiSuggestion();
        suggestion.setId(10L);
        suggestion.setTicket(ticket);
        suggestion.setSuggestedCategory(TicketCategory.SOFTWARE);
        suggestion.setSuggestedPriority(TicketPriority.P3);
        suggestion.setIsAccepted(false);
        suggestion.setIsOverridden(false);

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(suggestionRepository.findById(10L)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OverrideAiSuggestionRequest overrideReq = new OverrideAiSuggestionRequest();
        overrideReq.setNewCategory(TicketCategory.OUTAGE);
        overrideReq.setNewPriority(TicketPriority.P1);
        overrideReq.setReason("Affects all month-end financial billing closing operations");

        AiTriageResponseDTO response = aiTriageService.overrideSuggestion(5L, 10L, overrideReq);

        assertTrue(response.getIsOverridden());
        assertFalse(response.getIsAccepted());
        assertEquals("Affects all month-end financial billing closing operations", response.getHumanOverrideReason());
        assertEquals(TicketPriority.P1, ticket.getPriority());
        assertEquals(TicketCategory.OUTAGE, ticket.getCategory());
    }
}
