package com.sentineldesk;

import com.sentineldesk.attachment.AttachmentDTO;
import com.sentineldesk.attachment.AttachmentRepository;
import com.sentineldesk.attachment.AttachmentService;
import com.sentineldesk.common.exception.BadRequestException;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.incident.IncidentRepository;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.Role;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttachmentServiceTest {

    private AttachmentRepository attachmentRepository;
    private TicketRepository ticketRepository;
    private IncidentRepository incidentRepository;
    private UserRepository userRepository;
    private AuditService auditService;
    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentRepository = mock(AttachmentRepository.class);
        ticketRepository = mock(TicketRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        userRepository = mock(UserRepository.class);
        auditService = mock(AuditService.class);

        attachmentService = new AttachmentService(
                attachmentRepository, ticketRepository, incidentRepository, userRepository, auditService
        );
    }

    @Test
    @DisplayName("Upload rejects forbidden executable file extensions")
    void testRejectForbiddenExtensions() {
        Ticket ticket = new Ticket("SD-2026-000001", "Issue", "Desc", null, TicketCategory.SOFTWARE, TicketPriority.P3);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        MockMultipartFile dangerousFile = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", "malicious payload".getBytes()
        );

        assertThrows(BadRequestException.class, () -> {
            attachmentService.uploadTicketAttachment(1L, dangerousFile);
        });

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upload accepts valid log and image attachments")
    void testAcceptValidAttachments() {
        Ticket ticket = new Ticket("SD-2026-000001", "Issue", "Desc", null, TicketCategory.SOFTWARE, TicketPriority.P3);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(attachmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile logFile = new MockMultipartFile(
                "file", "system_crash.log", "text/plain", "Error at line 42: NullPointerException".getBytes()
        );

        AttachmentDTO dto = attachmentService.uploadTicketAttachment(1L, logFile);

        assertNotNull(dto);
        assertEquals("system_crash.log", dto.getFilename());
        assertEquals(logFile.getSize(), dto.getFileSize());
        verify(attachmentRepository, times(1)).save(any());
    }
}
