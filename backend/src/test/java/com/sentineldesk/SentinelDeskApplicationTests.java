package com.sentineldesk;

import com.sentineldesk.sla.SlaPolicyRepository;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SentinelDeskApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlaPolicyRepository slaPolicyRepository;

    @Test
    @DisplayName("Application context loads with Flyway migrations and seed data")
    void contextLoads() {
        assertTrue(userRepository.count() >= 4, "Users table must be populated by Flyway migrations and DataInitializer");
        assertTrue(userRepository.findByEmail("admin@sentineldesk.com").isPresent());
        assertTrue(userRepository.findByEmail("admin@sentineldesk.com").get().getRole() == Role.ADMIN);

        assertTrue(slaPolicyRepository.findByPriority(TicketPriority.P1).isPresent(), "P1 SLA Policy must be seeded");
        assertTrue(slaPolicyRepository.findByPriority(TicketPriority.P2).isPresent(), "P2 SLA Policy must be seeded");
    }
}
