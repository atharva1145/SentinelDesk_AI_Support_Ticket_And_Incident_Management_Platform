package com.sentineldesk.incident;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
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

    private IncidentDTO populateLinkedTickets(Incident incident) {
        IncidentDTO dto = new IncidentDTO(incident);
        List<IncidentTicket> links = incidentTicketRepository.findByIncidentIdWithTickets(incident.getId());
        List<Long> ids = links.stream().map(l -> l.getTicket().getId()).collect(Collectors.toList());
        dto.setLinkedTicketIds(ids);
        return dto;
    }
}
