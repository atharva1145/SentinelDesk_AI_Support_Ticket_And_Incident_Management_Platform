package com.sentineldesk.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.user.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class AiTriageResponseDTO {
    private Long id;
    private Long ticketId;
    private TicketCategory suggestedCategory;
    private TicketPriority suggestedPriority;
    private String summary;
    private String suggestedDomain;
    private String suggestedTeam;
    private List<String> diagnosticQuestions;
    private List<String> suggestedActions;
    private String priorityExplanation;
    private BigDecimal confidenceEstimate;
    private String modelProvider;
    private String modelName;
    private Boolean isAccepted;
    private Boolean isOverridden;
    private String humanOverrideReason;
    private UserDTO overriddenBy;
    private LocalDateTime createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AiTriageResponseDTO() {}

    public AiTriageResponseDTO(TicketAiSuggestion sugg) {
        this.id = sugg.getId();
        this.ticketId = sugg.getTicket().getId();
        this.suggestedCategory = sugg.getSuggestedCategory();
        this.suggestedPriority = sugg.getSuggestedPriority();
        this.summary = sugg.getSummary();
        this.suggestedDomain = sugg.getSuggestedDomain();
        this.suggestedTeam = sugg.getSuggestedTeam();
        this.priorityExplanation = sugg.getPriorityExplanation();
        this.confidenceEstimate = sugg.getConfidenceEstimate();
        this.modelProvider = sugg.getModelProvider();
        this.modelName = sugg.getModelName();
        this.isAccepted = sugg.getIsAccepted();
        this.isOverridden = sugg.getIsOverridden();
        this.humanOverrideReason = sugg.getHumanOverrideReason();
        this.overriddenBy = UserDTO.fromEntity(sugg.getOverriddenBy());
        this.createdAt = sugg.getCreatedAt();

        try {
            if (sugg.getDiagnosticQuestionsJson() != null) {
                this.diagnosticQuestions = objectMapper.readValue(sugg.getDiagnosticQuestionsJson(), new TypeReference<>() {});
            } else {
                this.diagnosticQuestions = Collections.emptyList();
            }
        } catch (Exception e) {
            this.diagnosticQuestions = Collections.emptyList();
        }

        try {
            if (sugg.getSuggestedActionsJson() != null) {
                this.suggestedActions = objectMapper.readValue(sugg.getSuggestedActionsJson(), new TypeReference<>() {});
            } else {
                this.suggestedActions = Collections.emptyList();
            }
        } catch (Exception e) {
            this.suggestedActions = Collections.emptyList();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public TicketCategory getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(TicketCategory suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public TicketPriority getSuggestedPriority() {
        return suggestedPriority;
    }

    public void setSuggestedPriority(TicketPriority suggestedPriority) {
        this.suggestedPriority = suggestedPriority;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSuggestedDomain() {
        return suggestedDomain;
    }

    public void setSuggestedDomain(String suggestedDomain) {
        this.suggestedDomain = suggestedDomain;
    }

    public String getSuggestedTeam() {
        return suggestedTeam;
    }

    public void setSuggestedTeam(String suggestedTeam) {
        this.suggestedTeam = suggestedTeam;
    }

    public List<String> getDiagnosticQuestions() {
        return diagnosticQuestions;
    }

    public void setDiagnosticQuestions(List<String> diagnosticQuestions) {
        this.diagnosticQuestions = diagnosticQuestions;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public String getPriorityExplanation() {
        return priorityExplanation;
    }

    public void setPriorityExplanation(String priorityExplanation) {
        this.priorityExplanation = priorityExplanation;
    }

    public BigDecimal getConfidenceEstimate() {
        return confidenceEstimate;
    }

    public void setConfidenceEstimate(BigDecimal confidenceEstimate) {
        this.confidenceEstimate = confidenceEstimate;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Boolean getIsOverridden() {
        return isOverridden;
    }

    public void setIsOverridden(Boolean overridden) {
        isOverridden = overridden;
    }

    public String getHumanOverrideReason() {
        return humanOverrideReason;
    }

    public void setHumanOverrideReason(String humanOverrideReason) {
        this.humanOverrideReason = humanOverrideReason;
    }

    public UserDTO getOverriddenBy() {
        return overriddenBy;
    }

    public void setOverriddenBy(UserDTO overriddenBy) {
        this.overriddenBy = overriddenBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
