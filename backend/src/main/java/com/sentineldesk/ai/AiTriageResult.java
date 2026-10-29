package com.sentineldesk.ai;

import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AiTriageResult {
    private TicketCategory category;
    private TicketPriority suggestedPriority;
    private String summary;
    private String suggestedDomain;
    private String suggestedTeam;
    private List<String> diagnosticQuestions = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private String priorityExplanation;
    private BigDecimal confidenceEstimate;
    private String modelProvider;
    private String modelName;

    public AiTriageResult() {}

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
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
}
