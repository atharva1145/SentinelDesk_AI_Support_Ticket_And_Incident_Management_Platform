package com.sentineldesk;

import com.sentineldesk.ticket.TicketNumberGenerator;
import com.sentineldesk.ticket.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConcurrentTicketNumberTest {

    @Test
    @DisplayName("Ticket number generation produces unique sequential numbers under concurrent requests")
    void testConcurrentTicketNumberGeneration() throws InterruptedException {
        TicketRepository repository = mock(TicketRepository.class);
        when(repository.count()).thenReturn(0L);

        TicketNumberGenerator generator = new TicketNumberGenerator(repository);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> generatedNumbers = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    String number = generator.nextTicketNumber();
                    generatedNumbers.add(number);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // All 50 ticket numbers must be completely unique
        assertEquals(50, generatedNumbers.size(), "Every ticket number generated under concurrency must be unique");
    }
}
