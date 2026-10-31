package com.sentineldesk.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    List<Attachment> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
