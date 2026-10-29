package com.sentineldesk.escalation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sentineldesk.sla.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final EscalationService escalationService;

    public EscalationScheduler(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @Scheduled(fixedDelayString = "${sentineldesk.sla.scheduler.interval-ms:30000}")
    public void runSlaCheck() {
        try {
            int triggered = escalationService.checkAndProcessEscalations();
            if (triggered > 0) {
                log.info("SLA background check completed: {} new escalations triggered", triggered);
            }
        } catch (Exception ex) {
            log.error("Error during SLA check execution: {}", ex.getMessage(), ex);
        }
    }
}
