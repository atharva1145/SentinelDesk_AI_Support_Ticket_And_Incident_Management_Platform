package com.sentineldesk.ai;

import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_ai_suggestions")
public class TicketAiSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_category")
    private TicketCategory suggestedCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_priority")
    private TicketPriority suggestedPriority;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "suggested_domain")
    private String suggestedDomain;

    @Column(name = "suggested_team")
    private String suggestedTeam;

    @Column(name = "diagnostic_questions_json", columnDefinition = "TEXT")
    private String diagnosticQuestionsJson;

    @Column(name = "suggested_actions_json", columnDefinition = "TEXT")
    private String suggestedActionsJson;

    @Column(name = "priority_explanation", columnDefinition = "TEXT")
    private String priorityExplanation;

    @Column(name = "confidence_estimate", precision = 5, scale = 2)
    private BigDecimal confidenceEstimate;

    @Column(name = "model_provider")
    private String modelProvider;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "is_accepted")
    private Boolean isAccepted = false;

    @Column(name = "is_overridden")
    private Boolean isOverridden = false;

    @Column(name = "human_override_reason", columnDefinition = "TEXT")
    private String humanOverrideReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by_id")
    private User overriddenBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TicketAiSuggestion() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
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

    public String getDiagnosticQuestionsJson() {
        return diagnosticQuestionsJson;
    }

    public void setDiagnosticQuestionsJson(String diagnosticQuestionsJson) {
        this.diagnosticQuestionsJson = diagnosticQuestionsJson;
    }

    public String getSuggestedActionsJson() {
        return suggestedActionsJson;
    }

    public void setSuggestedActionsJson(String suggestedActionsJson) {
        this.suggestedActionsJson = suggestedActionsJson;
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

    public User getOverriddenBy() {
        return overriddenBy;
    }

    public void setOverriddenBy(User overriddenBy) {
        this.overriddenBy = overriddenBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
