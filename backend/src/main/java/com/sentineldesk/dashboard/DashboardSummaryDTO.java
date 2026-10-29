package com.sentineldesk.dashboard;

import java.math.BigDecimal;

public class DashboardSummaryDTO {
    private long totalOpenTickets;
    private long unassignedTickets;
    private long criticalAndHighTickets;
    private long ticketsApproachingSla;
    private long slaBreachCount;
    private long ticketsResolvedToday;
    private double avgFirstResponseMinutes;
    private double avgResolutionHours;
    private double slaComplianceRatePercent;
    private long activeIncidentsCount;

    public DashboardSummaryDTO() {}

    public long getTotalOpenTickets() {
        return totalOpenTickets;
    }

    public void setTotalOpenTickets(long totalOpenTickets) {
        this.totalOpenTickets = totalOpenTickets;
    }

    public long getUnassignedTickets() {
        return unassignedTickets;
    }

    public void setUnassignedTickets(long unassignedTickets) {
        this.unassignedTickets = unassignedTickets;
    }

    public long getCriticalAndHighTickets() {
        return criticalAndHighTickets;
    }

    public void setCriticalAndHighTickets(long criticalAndHighTickets) {
        this.criticalAndHighTickets = criticalAndHighTickets;
    }

    public long getTicketsApproachingSla() {
        return ticketsApproachingSla;
    }

    public void setTicketsApproachingSla(long ticketsApproachingSla) {
        this.ticketsApproachingSla = ticketsApproachingSla;
    }

    public long getSlaBreachCount() {
        return slaBreachCount;
    }

    public void setSlaBreachCount(long slaBreachCount) {
        this.slaBreachCount = slaBreachCount;
    }

    public long getTicketsResolvedToday() {
        return ticketsResolvedToday;
    }

    public void setTicketsResolvedToday(long ticketsResolvedToday) {
        this.ticketsResolvedToday = ticketsResolvedToday;
    }

    public double getAvgFirstResponseMinutes() {
        return avgFirstResponseMinutes;
    }

    public void setAvgFirstResponseMinutes(double avgFirstResponseMinutes) {
        this.avgFirstResponseMinutes = avgFirstResponseMinutes;
    }

    public double getAvgResolutionHours() {
        return avgResolutionHours;
    }

    public void setAvgResolutionHours(double avgResolutionHours) {
        this.avgResolutionHours = avgResolutionHours;
    }

    public double getSlaComplianceRatePercent() {
        return slaComplianceRatePercent;
    }

    public void setSlaComplianceRatePercent(double slaComplianceRatePercent) {
        this.slaComplianceRatePercent = slaComplianceRatePercent;
    }

    public long getActiveIncidentsCount() {
        return activeIncidentsCount;
    }

    public void setActiveIncidentsCount(long activeIncidentsCount) {
        this.activeIncidentsCount = activeIncidentsCount;
    }
}
