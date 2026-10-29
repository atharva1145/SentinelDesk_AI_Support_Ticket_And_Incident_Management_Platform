package com.sentineldesk.dashboard;

import com.sentineldesk.incident.IncidentRepository;
import com.sentineldesk.incident.IncidentStatus;
import com.sentineldesk.sla.SlaTracker;
import com.sentineldesk.sla.SlaTrackerRepository;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final SlaTrackerRepository slaTrackerRepository;
    private final IncidentRepository incidentRepository;

    public DashboardService(TicketRepository ticketRepository,
                            SlaTrackerRepository slaTrackerRepository,
                            IncidentRepository incidentRepository) {
        this.ticketRepository = ticketRepository;
        this.slaTrackerRepository = slaTrackerRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummaryMetrics() {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        summary.setTotalOpenTickets(ticketRepository.countOpenTickets());
        summary.setUnassignedTickets(ticketRepository.countUnassignedTickets());
        summary.setCriticalAndHighTickets(ticketRepository.countCriticalAndHighTickets());

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        summary.setTicketsResolvedToday(ticketRepository.countResolvedSince(startOfToday));

        // SLA Metrics
        List<SlaTracker> allTrackers = slaTrackerRepository.findAll();
        long breachedCount = 0;
        long approachingCount = 0;
        long resolvedTrackers = 0;
        long compliantTrackers = 0;

        for (SlaTracker tracker : allTrackers) {
            if (Boolean.TRUE.equals(tracker.getResolutionBreached()) || Boolean.TRUE.equals(tracker.getFirstResponseBreached())) {
                breachedCount++;
            }
            if (Boolean.TRUE.equals(tracker.getWarningIssued()) && !Boolean.TRUE.equals(tracker.getResolutionMet())) {
                approachingCount++;
            }
            if (Boolean.TRUE.equals(tracker.getResolutionMet())) {
                resolvedTrackers++;
                if (!Boolean.TRUE.equals(tracker.getResolutionBreached())) {
                    compliantTrackers++;
                }
            }
        }

        summary.setSlaBreachCount(breachedCount);
        summary.setTicketsApproachingSla(approachingCount);

        double complianceRate = resolvedTrackers > 0 ? ((double) compliantTrackers / resolvedTrackers) * 100.0 : 100.0;
        summary.setSlaComplianceRatePercent(Math.round(complianceRate * 10.0) / 10.0);

        // Compute average first response and resolution times from persisted tickets
        List<Ticket> allTickets = ticketRepository.findAll();

        double totalFirstResponseMinutes = 0;
        long responseSampleCount = 0;

        double totalResolutionHours = 0;
        long resolutionSampleCount = 0;

        for (Ticket ticket : allTickets) {
            if (ticket.getFirstResponseAt() != null && ticket.getCreatedAt() != null) {
                long minutes = Duration.between(ticket.getCreatedAt(), ticket.getFirstResponseAt()).toMinutes();
                if (minutes >= 0) {
                    totalFirstResponseMinutes += minutes;
                    responseSampleCount++;
                }
            }
            if (ticket.getResolvedAt() != null && ticket.getCreatedAt() != null) {
                double hours = Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt()).toMinutes() / 60.0;
                if (hours >= 0) {
                    totalResolutionHours += hours;
                    resolutionSampleCount++;
                }
            }
        }

        double avgResponse = responseSampleCount > 0 ? totalFirstResponseMinutes / responseSampleCount : 0.0;
        double avgResolution = resolutionSampleCount > 0 ? totalResolutionHours / resolutionSampleCount : 0.0;

        summary.setAvgFirstResponseMinutes(Math.round(avgResponse * 10.0) / 10.0);
        summary.setAvgResolutionHours(Math.round(avgResolution * 10.0) / 10.0);

        long activeIncidents = incidentRepository.count() - incidentRepository.countByStatus(IncidentStatus.RESOLVED);
        summary.setActiveIncidentsCount(Math.max(0, activeIncidents));

        return summary;
    }

    @Transactional(readOnly = true)
    public DashboardChartsDTO getChartData() {
        DashboardChartsDTO dto = new DashboardChartsDTO();

        // 1. By Priority
        List<Object[]> priorityRows = ticketRepository.countByPriorityGroup();
        List<Map<String, Object>> priorityList = priorityRows.stream()
                .map(r -> Map.<String, Object>of("name", r[0].toString(), "count", r[1]))
                .collect(Collectors.toList());
        dto.setTicketsByPriority(priorityList);

        // 2. By Category
        List<Object[]> catRows = ticketRepository.countByCategoryGroup();
        List<Map<String, Object>> catList = catRows.stream()
                .map(r -> Map.<String, Object>of("name", r[0].toString(), "count", r[1]))
                .collect(Collectors.toList());
        dto.setTicketsByCategory(catList);

        // 3. By Status
        List<Object[]> statusRows = ticketRepository.countByStatusGroup();
        List<Map<String, Object>> statusList = statusRows.stream()
                .map(r -> Map.<String, Object>of("name", r[0].toString(), "count", r[1]))
                .collect(Collectors.toList());
        dto.setTicketsByStatus(statusList);

        // 4. Agent Workload
        List<Object[]> agentRows = ticketRepository.countWorkloadByAgent();
        List<Map<String, Object>> agentList = agentRows.stream()
                .map(r -> Map.<String, Object>of("agentId", r[0], "agentName", r[1], "activeTickets", r[2]))
                .collect(Collectors.toList());
        dto.setAgentWorkload(agentList);

        // 5. Team Workload
        List<Object[]> teamRows = ticketRepository.countWorkloadByTeam();
        List<Map<String, Object>> teamList = teamRows.stream()
                .map(r -> Map.<String, Object>of("teamId", r[0], "teamName", r[1], "activeTickets", r[2]))
                .collect(Collectors.toList());
        dto.setTeamWorkload(teamList);

        // 6. Volume over the last 7 days
        List<Map<String, Object>> volumeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate today = LocalDate.now();

        List<Ticket> tickets = ticketRepository.findAll();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = date.format(formatter);
            long count = tickets.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(date))
                    .count();
            volumeList.add(Map.of("date", label, "tickets", count));
        }
        dto.setVolumeOverTime(volumeList);

        return dto;
    }
}
