package com.sentineldesk.sla;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);

    private final SlaPolicyRepository policyRepository;
    private final SlaTrackerRepository trackerRepository;

    public SlaService(SlaPolicyRepository policyRepository, SlaTrackerRepository trackerRepository) {
        this.policyRepository = policyRepository;
        this.trackerRepository = trackerRepository;
    }

    @Transactional
    public SlaTracker initTrackerForTicket(Ticket ticket) {
        SlaPolicy policy = policyRepository.findByPriority(ticket.getPriority())
                .orElseGet(() -> policyRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No SLA Policy configured")));

        long firstResponseMinutes = (long) (policy.getFirstResponseHours().doubleValue() * 60);
        long resolutionMinutes = (long) (policy.getResolutionHours().doubleValue() * 60);

        LocalDateTime baseTime = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
        LocalDateTime firstResponseDeadline = baseTime.plusMinutes(firstResponseMinutes);
        LocalDateTime resolutionDeadline = baseTime.plusMinutes(resolutionMinutes);

        ticket.setSlaDeadline(resolutionDeadline);

        SlaTracker tracker = new SlaTracker(ticket, policy, firstResponseDeadline, resolutionDeadline);
        return trackerRepository.save(tracker);
    }

    @Transactional
    public void recordFirstResponse(Ticket ticket) {
        trackerRepository.findByTicketId(ticket.getId()).ifPresent(tracker -> {
            if (!Boolean.TRUE.equals(tracker.getFirstResponseMet())) {
                LocalDateTime now = LocalDateTime.now();
                tracker.setFirstResponseMet(true);
                tracker.setFirstResponseBreached(now.isAfter(tracker.getFirstResponseDeadline()));
                ticket.setFirstResponseAt(now);
                trackerRepository.save(tracker);
                log.info("Recorded first response for ticket {}. Met: {}, Breached: {}",
                        ticket.getTicketNumber(), tracker.getFirstResponseMet(), tracker.getFirstResponseBreached());
            }
        });
    }

    @Transactional
    public void recordResolution(Ticket ticket) {
        trackerRepository.findByTicketId(ticket.getId()).ifPresent(tracker -> {
            if (!Boolean.TRUE.equals(tracker.getResolutionMet())) {
                LocalDateTime now = LocalDateTime.now();
                tracker.setResolutionMet(true);
                tracker.setResolutionBreached(now.isAfter(tracker.getResolutionDeadline()));
                ticket.setResolvedAt(now);
                trackerRepository.save(tracker);
                log.info("Recorded resolution for ticket {}. Met: {}, Breached: {}",
                        ticket.getTicketNumber(), tracker.getResolutionMet(), tracker.getResolutionBreached());
            }
        });
    }

    @Transactional
    public void updateTicketPrioritySla(Ticket ticket, TicketPriority newPriority) {
        trackerRepository.findByTicketId(ticket.getId()).ifPresent(tracker -> {
            SlaPolicy newPolicy = policyRepository.findByPriority(newPriority)
                    .orElse(tracker.getPolicy());
            tracker.setPolicy(newPolicy);

            LocalDateTime baseTime = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
            long resolutionMinutes = (long) (newPolicy.getResolutionHours().doubleValue() * 60);
            LocalDateTime newResDeadline = baseTime.plusMinutes(resolutionMinutes);

            tracker.setResolutionDeadline(newResDeadline);
            ticket.setSlaDeadline(newResDeadline);
            trackerRepository.save(tracker);
        });
    }

    @Transactional(readOnly = true)
    public SlaTrackerDTO getTrackerForTicket(Long ticketId) {
        return trackerRepository.findByTicketId(ticketId)
                .map(SlaTrackerDTO::new)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyDTO> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(SlaPolicyDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlaPolicyDTO updatePolicy(Long policyId, SlaPolicyDTO dto) {
        SlaPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

        if (dto.getName() != null) policy.setName(dto.getName());
        if (dto.getFirstResponseHours() != null) policy.setFirstResponseHours(dto.getFirstResponseHours());
        if (dto.getResolutionHours() != null) policy.setResolutionHours(dto.getResolutionHours());
        if (dto.getDescription() != null) policy.setDescription(dto.getDescription());
        policy.setActive(dto.isActive());

        policy = policyRepository.save(policy);
        return new SlaPolicyDTO(policy);
    }
}
