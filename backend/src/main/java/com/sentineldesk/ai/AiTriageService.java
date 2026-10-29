package com.sentineldesk.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.team.Team;
import com.sentineldesk.team.TeamRepository;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiTriageService {

    private static final Logger log = LoggerFactory.getLogger(AiTriageService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AiProviderClient aiProviderClient;
    private final TicketAiSuggestionRepository suggestionRepository;
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AiTriageService(AiProviderClient aiProviderClient,
                           TicketAiSuggestionRepository suggestionRepository,
                           TicketRepository ticketRepository,
                           TeamRepository teamRepository,
                           UserRepository userRepository,
                           AuditService auditService) {
        this.aiProviderClient = aiProviderClient;
        this.suggestionRepository = suggestionRepository;
        this.ticketRepository = ticketRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public TicketAiSuggestion generateTriage(Ticket ticket) {
        try {
            AiTriageResult result = aiProviderClient.classifyTicket(ticket.getTitle(), ticket.getDescription());
            if (result == null) {
                log.warn("AI Triage returned null result for ticket #{}", ticket.getTicketNumber());
                return null;
            }

            TicketAiSuggestion suggestion = new TicketAiSuggestion();
            suggestion.setTicket(ticket);
            suggestion.setSuggestedCategory(result.getCategory());
            suggestion.setSuggestedPriority(result.getSuggestedPriority());
            suggestion.setSummary(result.getSummary());
            suggestion.setSuggestedDomain(result.getSuggestedDomain());
            suggestion.setSuggestedTeam(result.getSuggestedTeam());
            suggestion.setPriorityExplanation(result.getPriorityExplanation());
            suggestion.setConfidenceEstimate(result.getConfidenceEstimate());
            suggestion.setModelProvider(result.getModelProvider());
            suggestion.setModelName(result.getModelName());

            suggestion.setDiagnosticQuestionsJson(objectMapper.writeValueAsString(result.getDiagnosticQuestions()));
            suggestion.setSuggestedActionsJson(objectMapper.writeValueAsString(result.getSuggestedActions()));

            suggestion = suggestionRepository.save(suggestion);
            log.info("Persisted AI suggestion #{} for ticket #{}", suggestion.getId(), ticket.getTicketNumber());
            return suggestion;
        } catch (Exception ex) {
            // CRITICAL REQUIREMENT: AI failure must never fail ticket creation or corrupt ticket data
            log.error("AI Triage encountered an error for ticket {}: {}", ticket.getTicketNumber(), ex.getMessage());
            return null;
        }
    }

    @Transactional
    public AiTriageResponseDTO acceptSuggestion(Long ticketId, Long suggestionId) {
        TicketAiSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("AI Suggestion not found: " + suggestionId));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Apply suggestions to ticket
        ticket.setCategory(suggestion.getSuggestedCategory());
        ticket.setPriority(suggestion.getSuggestedPriority());

        // Attempt team auto-assign if team matches
        if (suggestion.getSuggestedTeam() != null) {
            teamRepository.findByName(suggestion.getSuggestedTeam()).ifPresent(ticket::setAssignedTeam);
        }

        suggestion.setIsAccepted(true);
        suggestion.setIsOverridden(false);
        ticketRepository.save(ticket);
        suggestionRepository.save(suggestion);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditService.logSync(currentUserId, "AI_SUGGESTION_ACCEPTED", "TICKET", ticket.getId().toString(),
                "Accepted AI triage recommendation for category: " + suggestion.getSuggestedCategory() + ", priority: " + suggestion.getSuggestedPriority(), null);

        return new AiTriageResponseDTO(suggestion);
    }

    @Transactional
    public AiTriageResponseDTO overrideSuggestion(Long ticketId, Long suggestionId, OverrideAiSuggestionRequest request) {
        TicketAiSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("AI Suggestion not found: " + suggestionId));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setCategory(request.getNewCategory());
        ticket.setPriority(request.getNewPriority());

        if (request.getNewTeamId() != null) {
            Team team = teamRepository.findById(request.getNewTeamId()).orElse(null);
            ticket.setAssignedTeam(team);
        }

        suggestion.setIsOverridden(true);
        suggestion.setIsAccepted(false);
        suggestion.setHumanOverrideReason(request.getReason());
        suggestion.setOverriddenBy(currentUser);

        ticketRepository.save(ticket);
        suggestion = suggestionRepository.save(suggestion);

        auditService.logSync(currentUserId, "AI_SUGGESTION_OVERRIDDEN", "TICKET", ticket.getId().toString(),
                "Overrode AI suggestion. New Category: " + request.getNewCategory() +
                ", New Priority: " + request.getNewPriority() + ". Reason: " + request.getReason(), null);

        return new AiTriageResponseDTO(suggestion);
    }

    @Transactional(readOnly = true)
    public List<AiTriageResponseDTO> getSuggestionsForTicket(Long ticketId) {
        return suggestionRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(AiTriageResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AiTriageResponseDTO getLatestSuggestionForTicket(Long ticketId) {
        return suggestionRepository.findFirstByTicketIdOrderByCreatedAtDesc(ticketId)
                .map(AiTriageResponseDTO::new)
                .orElse(null);
    }
}
