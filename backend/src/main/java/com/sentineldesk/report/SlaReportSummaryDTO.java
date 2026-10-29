package com.sentineldesk.report;

import java.util.Map;

public class SlaReportSummaryDTO {

    private long totalTickets;
    private long compliantTickets;
    private long breachedTickets;
    private double overallComplianceRate;
    private double firstResponseComplianceRate;
    private double resolutionComplianceRate;
    private long meanTimeToAcknowledgeMinutes;
    private long meanTimeToResolveMinutes;
    private Double averageCsatRating;
    private long totalRatings;
    private Map<String, Object> priorityBreakdown;

    public SlaReportSummaryDTO() {}

    public SlaReportSummaryDTO(long totalTickets, long compliantTickets, long breachedTickets,
                               double overallComplianceRate, double firstResponseComplianceRate,
                               double resolutionComplianceRate, long meanTimeToAcknowledgeMinutes,
                               long meanTimeToResolveMinutes, Double averageCsatRating, long totalRatings,
                               Map<String, Object> priorityBreakdown) {
        this.totalTickets = totalTickets;
        this.compliantTickets = compliantTickets;
        this.breachedTickets = breachedTickets;
        this.overallComplianceRate = overallComplianceRate;
        this.firstResponseComplianceRate = firstResponseComplianceRate;
        this.resolutionComplianceRate = resolutionComplianceRate;
        this.meanTimeToAcknowledgeMinutes = meanTimeToAcknowledgeMinutes;
        this.meanTimeToResolveMinutes = meanTimeToResolveMinutes;
        this.averageCsatRating = averageCsatRating;
        this.totalRatings = totalRatings;
        this.priorityBreakdown = priorityBreakdown;
    }

    public long getTotalTickets() { return totalTickets; }
    public long getCompliantTickets() { return compliantTickets; }
    public long getBreachedTickets() { return breachedTickets; }
    public double getOverallComplianceRate() { return overallComplianceRate; }
    public double getFirstResponseComplianceRate() { return firstResponseComplianceRate; }
    public double getResolutionComplianceRate() { return resolutionComplianceRate; }
    public long getMeanTimeToAcknowledgeMinutes() { return meanTimeToAcknowledgeMinutes; }
    public long getMeanTimeToResolveMinutes() { return meanTimeToResolveMinutes; }
    public Double getAverageCsatRating() { return averageCsatRating; }
    public long getTotalRatings() { return totalRatings; }
    public Map<String, Object> getPriorityBreakdown() { return priorityBreakdown; }
}
