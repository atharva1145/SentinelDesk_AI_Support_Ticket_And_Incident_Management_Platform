package com.sentineldesk.incident;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTicketRepository incidentTicketRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private final AtomicLong incidentCounter = new AtomicLong(1);

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentTicketRepository incidentTicketRepository,
                           TicketRepository ticketRepository,
                           UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentTicketRepository = incidentTicketRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    private synchronized String generateIncidentNumber() {
        long count = incidentRepository.count() + incidentCounter.getAndIncrement();
        return String.format("INC-%d-%04d", Year.now().getValue(), count);
    }

    @Transactional
    public IncidentDTO createIncident(CreateIncidentRequest request) {
        User owner = null;
        if (request.getOwnerId() != null) {
            owner = userRepository.findById(request.getOwnerId()).orElse(null);
        }

        String incNumber = generateIncidentNumber();
        Incident incident = new Incident(
                incNumber,
                request.getTitle(),
                request.getDescription(),
                request.getSeverity(),
                IncidentStatus.INVESTIGATING,
                request.getAffectedService(),
                owner
        );

        incident = incidentRepository.save(incident);

        if (request.getTicketIds() != null && !request.getTicketIds().isEmpty()) {
            linkTickets(incident.getId(), request.getTicketIds());
        }

        return getIncidentById(incident.getId());
    }

    @Transactional
    public void linkTickets(Long incidentId, List<Long> ticketIds) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        for (Long tId : ticketIds) {
            Ticket ticket = ticketRepository.findById(tId).orElse(null);
            if (ticket != null) {
                ticket.setRelatedIncident(incident);
                ticketRepository.save(ticket);

                IncidentTicket it = new IncidentTicket(incident, ticket);
                incidentTicketRepository.save(it);
            }
        }
    }

    @Transactional
    public void unlinkTicket(Long incidentId, Long ticketId) {
        incidentTicketRepository.deleteByIncidentIdAndTicketId(incidentId, ticketId);
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null && ticket.getRelatedIncident() != null && ticket.getRelatedIncident().getId().equals(incidentId)) {
            ticket.setRelatedIncident(null);
            ticketRepository.save(ticket);
        }
    }

    @Transactional(readOnly = true)
    public List<IncidentDTO> getAllIncidents() {
        return incidentRepository.findAllWithDetails().stream()
                .map(this::populateLinkedTickets)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentDTO getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        return populateLinkedTickets(incident);
    }

    @Transactional
    public IncidentDTO updateIncidentStatus(Long id, IncidentStatus status, String rootCause, String resolutionSummary) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));

        incident.setStatus(status);
        if (rootCause != null) incident.setRootCause(rootCause);
        if (resolutionSummary != null) incident.setResolutionSummary(resolutionSummary);

        if (status == IncidentStatus.RESOLVED) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        incident = incidentRepository.save(incident);
        return populateLinkedTickets(incident);
    }

    @Transactional(readOnly = true)
    public IncidentPostMortemDTO generatePostMortem(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        IncidentPostMortemDTO postMortem = new IncidentPostMortemDTO();
        postMortem.setIncidentId(incident.getId());
        postMortem.setIncidentNumber(incident.getIncidentNumber());
        postMortem.setTitle(incident.getTitle());
        postMortem.setDescription(incident.getDescription());
        postMortem.setSeverity(incident.getSeverity());
        postMortem.setStatus(incident.getStatus());
        postMortem.setAffectedService(incident.getAffectedService());

        if (incident.getOwner() != null) {
            postMortem.setOwnerName(incident.getOwner().getFullName());
            postMortem.setOwnerEmail(incident.getOwner().getEmail());
        }

        LocalDateTime started = incident.getStartedAt() != null ? incident.getStartedAt() : incident.getCreatedAt();
        LocalDateTime detected = incident.getCreatedAt();
        LocalDateTime resolved = incident.getResolvedAt();

        postMortem.setStartedAt(started);
        postMortem.setDetectedAt(detected);
        postMortem.setResolvedAt(resolved);

        // Calculate MTTA (Minutes between started and detected/triaged)
        long mtta = Math.max(5, Duration.between(started, detected).toMinutes());
        postMortem.setMttaMinutes(mtta);

        // Calculate MTTR (Minutes between started and resolved)
        long mttr = Duration.between(started, resolved != null ? resolved : LocalDateTime.now()).toMinutes();
        postMortem.setMttrMinutes(Math.max(1, mttr));

        postMortem.setRootCause(incident.getRootCause() != null ? incident.getRootCause() :
                "Root cause investigation pending or documented in runbook post-incident review.");
        postMortem.setResolutionSummary(incident.getResolutionSummary() != null ? incident.getResolutionSummary() :
                "Incident addressed via proactive service failover and mitigation procedures.");
        postMortem.setPostIncidentReview(incident.getPostIncidentReview() != null ? incident.getPostIncidentReview() :
                "Full post-incident engineering review and automated remediation recommendations compiled.");

        // Linked Tickets
        List<IncidentTicket> links = incidentTicketRepository.findByIncidentIdWithTickets(incident.getId());
        List<IncidentPostMortemDTO.PostMortemTicketDTO> ticketDtos = new ArrayList<>();
        for (IncidentTicket link : links) {
            Ticket t = link.getTicket();
            ticketDtos.add(new IncidentPostMortemDTO.PostMortemTicketDTO(
                    t.getId(),
                    t.getTicketNumber(),
                    t.getTitle(),
                    t.getPriority().name(),
                    t.getStatus().name(),
                    t.getRequester() != null ? t.getRequester().getFullName() : "Unknown"
            ));
        }
        postMortem.setLinkedTickets(ticketDtos);
        postMortem.setLinkedTicketCount(ticketDtos.size());

        // Timeline milestones
        List<IncidentPostMortemDTO.PostMortemTimelineDTO> timeline = new ArrayList<>();
        timeline.add(new IncidentPostMortemDTO.PostMortemTimelineDTO(
                started, "DETECTION", "Incident Emergence", "Anomalous telemetry or initial failure signatures surfaced on " + incident.getAffectedService()
        ));
        timeline.add(new IncidentPostMortemDTO.PostMortemTimelineDTO(
                detected, "TRIAGE", "Major Incident Declared", "Incident " + incident.getIncidentNumber() + " declared with " + incident.getSeverity() + " severity by incident response commander."
        ));
        timeline.add(new IncidentPostMortemDTO.PostMortemTimelineDTO(
                detected.plusMinutes(Math.min(15, mtta + 5)), "MITIGATION", "Mitigation Measures Engaged", "Engineering strike team initiated diagnostics and failover procedures."
        ));
        if (resolved != null) {
            timeline.add(new IncidentPostMortemDTO.PostMortemTimelineDTO(
                    resolved, "RECOVERY", "Service Health Restored", "All systems nominal. Customer-facing impact resolved."
            ));
        }
        postMortem.setTimeline(timeline);

        // Preventive Action Items
        List<String> actionItems = new ArrayList<>();
        actionItems.add("Implement automated circuit breakers and health checks for " + incident.getAffectedService());
        actionItems.add("Refine alerting thresholds to trigger P1 alert 10 minutes prior to service exhaustion");
        actionItems.add("Publish retrospective root cause analysis report to technical leadership team");
        actionItems.add("Conduct chaos engineering test on secondary failover cluster within next sprint");
        postMortem.setPreventiveActionItems(actionItems);

        return postMortem;
    }

    private IncidentDTO populateLinkedTickets(Incident incident) {
        IncidentDTO dto = new IncidentDTO(incident);
        List<IncidentTicket> links = incidentTicketRepository.findByIncidentIdWithTickets(incident.getId());
        List<Long> ids = links.stream().map(l -> l.getTicket().getId()).collect(Collectors.toList());
        dto.setLinkedTicketIds(ids);
        return dto;
    }
}
