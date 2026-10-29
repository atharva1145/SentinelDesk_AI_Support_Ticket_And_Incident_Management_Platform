package com.sentineldesk.ticket;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TicketNumberGenerator {

    private final AtomicLong counter = new AtomicLong(1);
    private final TicketRepository ticketRepository;

    public TicketNumberGenerator(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public synchronized String nextTicketNumber() {
        int currentYear = Year.now().getValue();
        long currentCount = ticketRepository.count();
        long sequence = Math.max(currentCount + 1, counter.getAndIncrement());
        return String.format("SD-%d-%06d", currentYear, sequence);
    }
}
