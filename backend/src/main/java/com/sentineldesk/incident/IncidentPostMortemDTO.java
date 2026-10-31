package com.sentineldesk.incident;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidentPostMortemDTO {

    private Long incidentId;
    private String incidentNumber;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String affectedService;
    private String ownerName;
    private String ownerEmail;

    private LocalDateTime startedAt;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;

    private Long mttaMinutes; // Mean Time to Acknowledge
    private Long mttrMinutes; // Mean Time to Resolve

    private String rootCause;
    private String resolutionSummary;
    private String postIncidentReview;

    private int linkedTicketCount;
    private List<PostMortemTicketDTO> linkedTickets = new ArrayList<>();
    private List<PostMortemTimelineDTO> timeline = new ArrayList<>();
    private List<String> preventiveActionItems = new ArrayList<>();

    public IncidentPostMortemDTO() {}

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Long getMttaMinutes() {
        return mttaMinutes;
    }

    public void setMttaMinutes(Long mttaMinutes) {
        this.mttaMinutes = mttaMinutes;
    }

    public Long getMttrMinutes() {
        return mttrMinutes;
    }

    public void setMttrMinutes(Long mttrMinutes) {
        this.mttrMinutes = mttrMinutes;
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

    public int getLinkedTicketCount() {
        return linkedTicketCount;
    }

    public void setLinkedTicketCount(int linkedTicketCount) {
        this.linkedTicketCount = linkedTicketCount;
    }

    public List<PostMortemTicketDTO> getLinkedTickets() {
        return linkedTickets;
    }

    public void setLinkedTickets(List<PostMortemTicketDTO> linkedTickets) {
        this.linkedTickets = linkedTickets;
    }

    public List<PostMortemTimelineDTO> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<PostMortemTimelineDTO> timeline) {
        this.timeline = timeline;
    }

    public List<String> getPreventiveActionItems() {
        return preventiveActionItems;
    }

    public void setPreventiveActionItems(List<String> preventiveActionItems) {
        this.preventiveActionItems = preventiveActionItems;
    }

    public static class PostMortemTicketDTO {
        private Long id;
        private String ticketNumber;
        private String title;
        private String priority;
        private String status;
        private String requesterName;

        public PostMortemTicketDTO() {}

        public PostMortemTicketDTO(Long id, String ticketNumber, String title, String priority, String status, String requesterName) {
            this.id = id;
            this.ticketNumber = ticketNumber;
            this.title = title;
            this.priority = priority;
            this.status = status;
            this.requesterName = requesterName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRequesterName() { return requesterName; }
        public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    }

    public static class PostMortemTimelineDTO {
        private LocalDateTime timestamp;
        private String phase;
        private String title;
        private String description;

        public PostMortemTimelineDTO() {}

        public PostMortemTimelineDTO(LocalDateTime timestamp, String phase, String title, String description) {
            this.timestamp = timestamp;
            this.phase = phase;
            this.title = title;
            this.description = description;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
