package com.sentineldesk.routing;

public class AutoAssignResultDTO {

    private Long ticketId;
    private String ticketNumber;
    private Long assignedAgentId;
    private String assignedAgentName;
    private String assignedAgentEmail;
    private Long assignedTeamId;
    private String assignedTeamName;
    private String strategy;
    private long agentActiveTickets;
    private String reason;

    public AutoAssignResultDTO() {}

    public AutoAssignResultDTO(Long ticketId, String ticketNumber, Long assignedAgentId,
                               String assignedAgentName, String assignedAgentEmail,
                               Long assignedTeamId, String assignedTeamName,
                               String strategy, long agentActiveTickets, String reason) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.assignedAgentEmail = assignedAgentEmail;
        this.assignedTeamId = assignedTeamId;
        this.assignedTeamName = assignedTeamName;
        this.strategy = strategy;
        this.agentActiveTickets = agentActiveTickets;
        this.reason = reason;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }
    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }
    public String getAssignedAgentEmail() { return assignedAgentEmail; }
    public void setAssignedAgentEmail(String assignedAgentEmail) { this.assignedAgentEmail = assignedAgentEmail; }
    public Long getAssignedTeamId() { return assignedTeamId; }
    public void setAssignedTeamId(Long assignedTeamId) { this.assignedTeamId = assignedTeamId; }
    public String getAssignedTeamName() { return assignedTeamName; }
    public void setAssignedTeamName(String assignedTeamName) { this.assignedTeamName = assignedTeamName; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public long getAgentActiveTickets() { return agentActiveTickets; }
    public void setAgentActiveTickets(long agentActiveTickets) { this.agentActiveTickets = agentActiveTickets; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
