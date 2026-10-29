package com.sentineldesk.ticket;

public class AssignTicketRequest {
    private Long agentId;
    private Long teamId;
    private String reason;

    public AssignTicketRequest() {}

    public AssignTicketRequest(Long agentId, Long teamId, String reason) {
        this.agentId = agentId;
        this.teamId = teamId;
        this.reason = reason;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
