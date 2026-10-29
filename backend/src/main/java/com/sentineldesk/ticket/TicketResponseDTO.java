package com.sentineldesk.ticket;

import com.sentineldesk.ai.AiTriageResponseDTO;
import com.sentineldesk.incident.IncidentDTO;
import com.sentineldesk.sla.SlaTrackerDTO;
import com.sentineldesk.team.TeamDTO;
import com.sentineldesk.user.UserDTO;

import java.time.LocalDateTime;

public class TicketResponseDTO {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private UserDTO requester;
    private UserDTO assignedAgent;
    private TeamDTO assignedTeam;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private TicketSource source;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime slaDeadline;
    private String resolutionSummary;
    private IncidentDTO relatedIncident;
    private SlaTrackerDTO slaTracker;
    private AiTriageResponseDTO aiSuggestion;
    private Integer csatRating;
    private String csatFeedback;
    private Integer reopenedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketResponseDTO() {}

    public TicketResponseDTO(Ticket t) {
        this.id = t.getId();
        this.ticketNumber = t.getTicketNumber();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.requester = UserDTO.fromEntity(t.getRequester());
        this.assignedAgent = UserDTO.fromEntity(t.getAssignedAgent());
        this.assignedTeam = t.getAssignedTeam() != null ? new TeamDTO(t.getAssignedTeam()) : null;
        this.category = t.getCategory();
        this.priority = t.getPriority();
        this.status = t.getStatus();
        this.source = t.getSource();
        this.firstResponseAt = t.getFirstResponseAt();
        this.resolvedAt = t.getResolvedAt();
        this.closedAt = t.getClosedAt();
        this.slaDeadline = t.getSlaDeadline();
        this.resolutionSummary = t.getResolutionSummary();
        this.relatedIncident = t.getRelatedIncident() != null ? new IncidentDTO(t.getRelatedIncident()) : null;
        this.csatRating = t.getCsatRating();
        this.csatFeedback = t.getCsatFeedback();
        this.reopenedCount = t.getReopenedCount();
        this.createdAt = t.getCreatedAt();
        this.updatedAt = t.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserDTO getRequester() {
        return requester;
    }

    public void setRequester(UserDTO requester) {
        this.requester = requester;
    }

    public UserDTO getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(UserDTO assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public TeamDTO getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(TeamDTO assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketSource getSource() {
        return source;
    }

    public void setSource(TicketSource source) {
        this.source = source;
    }

    public LocalDateTime getFirstResponseAt() {
        return firstResponseAt;
    }

    public void setFirstResponseAt(LocalDateTime firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public IncidentDTO getRelatedIncident() {
        return relatedIncident;
    }

    public void setRelatedIncident(IncidentDTO relatedIncident) {
        this.relatedIncident = relatedIncident;
    }

    public SlaTrackerDTO getSlaTracker() {
        return slaTracker;
    }

    public void setSlaTracker(SlaTrackerDTO slaTracker) {
        this.slaTracker = slaTracker;
    }

    public AiTriageResponseDTO getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(AiTriageResponseDTO aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
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

    public Integer getCsatRating() {
        return csatRating;
    }

    public void setCsatRating(Integer csatRating) {
        this.csatRating = csatRating;
    }

    public String getCsatFeedback() {
        return csatFeedback;
    }

    public void setCsatFeedback(String csatFeedback) {
        this.csatFeedback = csatFeedback;
    }

    public Integer getReopenedCount() {
        return reopenedCount;
    }

    public void setReopenedCount(Integer reopenedCount) {
        this.reopenedCount = reopenedCount;
    }
}
