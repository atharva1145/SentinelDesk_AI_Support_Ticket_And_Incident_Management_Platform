package com.sentineldesk.attachment;

import com.sentineldesk.common.exception.BadRequestException;
import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.history.AuditService;
import com.sentineldesk.incident.Incident;
import com.sentineldesk.incident.IncidentRepository;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.ticket.Ticket;
import com.sentineldesk.ticket.TicketRepository;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "svg",
            "pdf", "log", "txt", "json", "csv", "xml", "md", "yaml", "yml",
            "zip", "tar", "gz"
    );

    private final Path storageDirectory;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             TicketRepository ticketRepository,
                             IncidentRepository incidentRepository,
                             UserRepository userRepository,
                             AuditService auditService) {
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;

        this.storageDirectory = Paths.get("uploads", "attachments").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            log.error("Could not initialize attachments storage directory", e);
        }
    }

    @Transactional
    public AttachmentDTO uploadTicketAttachment(Long ticketId, MultipartFile file) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        validateFile(file);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        if (SecurityUtils.isRequester() && currentUser != null && !ticket.getRequester().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You cannot upload attachments to other users' tickets.");
        }

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = this.storageDirectory.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file {}", originalName, e);
            throw new RuntimeException("Failed to store attachment file on disk", e);
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        Attachment attachment = new Attachment(ticket, null, originalName, storedFileName, file.getSize(), contentType, currentUser);
        attachment = attachmentRepository.save(attachment);

        auditService.logSync(currentUserId, "ATTACHMENT_UPLOAD", "TICKET", ticketId.toString(),
                "Uploaded attachment: " + originalName + " (" + (file.getSize() / 1024) + " KB)", null);

        log.info("Uploaded attachment #{} ({}) for ticket {}", attachment.getId(), originalName, ticket.getTicketNumber());
        return new AttachmentDTO(attachment);
    }

    @Transactional
    public AttachmentDTO uploadIncidentAttachment(Long incidentId, MultipartFile file) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        validateFile(file);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = this.storageDirectory.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store incident file {}", originalName, e);
            throw new RuntimeException("Failed to store attachment file on disk", e);
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        Attachment attachment = new Attachment(null, incident, originalName, storedFileName, file.getSize(), contentType, currentUser);
        attachment = attachmentRepository.save(attachment);

        auditService.logSync(currentUserId, "ATTACHMENT_UPLOAD", "INCIDENT", incidentId.toString(),
                "Uploaded incident attachment: " + originalName, null);

        log.info("Uploaded attachment #{} for incident {}", attachment.getId(), incident.getIncidentNumber());
        return new AttachmentDTO(attachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentDTO> getTicketAttachments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (SecurityUtils.isRequester()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (!ticket.getRequester().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Access denied: You cannot view attachments of other users' tickets.");
            }
        }

        return attachmentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(AttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttachmentDTO> getIncidentAttachments(Long incidentId) {
        return attachmentRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId).stream()
                .map(AttachmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentEntity(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
    }

    public Resource loadFileAsResource(Attachment attachment) {
        try {
            Path filePath = this.storageDirectory.resolve(attachment.getStoredPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Attachment file not found or unreadable on disk: " + attachment.getFilename());
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File path resolution error: " + attachment.getFilename());
        }
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = getAttachmentEntity(attachmentId);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = currentUserId != null && attachment.getUploadedBy() != null && currentUserId.equals(attachment.getUploadedBy().getId());
        boolean isStaff = SecurityUtils.isStaff();

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Access denied: You cannot delete this attachment.");
        }

        try {
            Path filePath = this.storageDirectory.resolve(attachment.getStoredPath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete physical attachment file {}: {}", attachment.getStoredPath(), e.getMessage());
        }

        attachmentRepository.delete(attachment);
        log.info("Deleted attachment #{}", attachmentId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 15MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BadRequestException("File must have a valid extension");
        }

        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported or restricted file type: ." + extension +
                    ". Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "attachment";
        String clean = Paths.get(filename).getFileName().toString();
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
