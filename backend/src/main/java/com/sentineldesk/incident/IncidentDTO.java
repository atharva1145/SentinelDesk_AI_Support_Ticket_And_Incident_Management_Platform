package com.sentineldesk.incident;

import com.sentineldesk.user.UserDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidentDTO {
    private Long id;
    private String incidentNumber;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String affectedService;
    private UserDTO owner;
    private String rootCause;
    private String resolutionSummary;
    private String postIncidentReview;
    private LocalDateTime startedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private List<Long> linkedTicketIds = new ArrayList<>();
    private int linkedTicketCount;

    public IncidentDTO() {}

    public IncidentDTO(Incident inc) {
        this.id = inc.getId();
        this.incidentNumber = inc.getIncidentNumber();
        this.title = inc.getTitle();
        this.description = inc.getDescription();
        this.severity = inc.getSeverity();
        this.status = inc.getStatus();
        this.affectedService = inc.getAffectedService();
        this.owner = UserDTO.fromEntity(inc.getOwner());
        this.rootCause = inc.getRootCause();
        this.resolutionSummary = inc.getResolutionSummary();
        this.postIncidentReview = inc.getPostIncidentReview();
        this.startedAt = inc.getStartedAt();
        this.resolvedAt = inc.getResolvedAt();
        this.createdAt = inc.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
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

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getAffectedService() {
        return affectedService;
    }

    public void setAffectedService(String affectedService) {
        this.affectedService = affectedService;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public String getPostIncidentReview() {
        return postIncidentReview;
    }

    public void setPostIncidentReview(String postIncidentReview) {
        this.postIncidentReview = postIncidentReview;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Long> getLinkedTicketIds() {
        return linkedTicketIds;
    }

    public void setLinkedTicketIds(List<Long> linkedTicketIds) {
        this.linkedTicketIds = linkedTicketIds;
        this.linkedTicketCount = linkedTicketIds != null ? linkedTicketIds.size() : 0;
    }

    public int getLinkedTicketCount() {
        return linkedTicketCount;
    }

    public void setLinkedTicketCount(int linkedTicketCount) {
        this.linkedTicketCount = linkedTicketCount;
    }
}
