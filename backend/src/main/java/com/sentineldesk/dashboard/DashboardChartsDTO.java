package com.sentineldesk.dashboard;

import java.util.List;
import java.util.Map;

public class DashboardChartsDTO {
    private List<Map<String, Object>> ticketsByPriority;
    private List<Map<String, Object>> ticketsByCategory;
    private List<Map<String, Object>> ticketsByStatus;
    private List<Map<String, Object>> agentWorkload;
    private List<Map<String, Object>> teamWorkload;
    private List<Map<String, Object>> volumeOverTime;

    public DashboardChartsDTO() {}

    public List<Map<String, Object>> getTicketsByPriority() {
        return ticketsByPriority;
    }

    public void setTicketsByPriority(List<Map<String, Object>> ticketsByPriority) {
        this.ticketsByPriority = ticketsByPriority;
    }

    public List<Map<String, Object>> getTicketsByCategory() {
        return ticketsByCategory;
    }

    public void setTicketsByCategory(List<Map<String, Object>> ticketsByCategory) {
        this.ticketsByCategory = ticketsByCategory;
    }

    public List<Map<String, Object>> getTicketsByStatus() {
        return ticketsByStatus;
    }

    public void setTicketsByStatus(List<Map<String, Object>> ticketsByStatus) {
        this.ticketsByStatus = ticketsByStatus;
    }

    public List<Map<String, Object>> getAgentWorkload() {
        return agentWorkload;
    }

    public void setAgentWorkload(List<Map<String, Object>> agentWorkload) {
        this.agentWorkload = agentWorkload;
    }

    public List<Map<String, Object>> getTeamWorkload() {
        return teamWorkload;
    }

    public void setTeamWorkload(List<Map<String, Object>> teamWorkload) {
        this.teamWorkload = teamWorkload;
    }

    public List<Map<String, Object>> getVolumeOverTime() {
        return volumeOverTime;
    }

    public void setVolumeOverTime(List<Map<String, Object>> volumeOverTime) {
        this.volumeOverTime = volumeOverTime;
    }
}
