package com.sentineldesk.attachment;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Attachment Management", description = "Ticket and Incident file attachments upload, listing, and streaming downloads")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/tickets/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment to a ticket")
    public ResponseEntity<ApiResponse<AttachmentDTO>> uploadTicketAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) {
        AttachmentDTO uploaded = attachmentService.uploadTicketAttachment(ticketId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("File uploaded successfully", uploaded));
    }

    @GetMapping("/tickets/{ticketId}/attachments")
    @Operation(summary = "Get all attachments for a ticket")
    public ResponseEntity<ApiResponse<List<AttachmentDTO>>> getTicketAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getTicketAttachments(ticketId)));
    }

    @PostMapping(value = "/incidents/{incidentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment to an incident")
    public ResponseEntity<ApiResponse<AttachmentDTO>> uploadIncidentAttachment(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file) {
        AttachmentDTO uploaded = attachmentService.uploadIncidentAttachment(incidentId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("File uploaded successfully", uploaded));
    }

    @GetMapping("/incidents/{incidentId}/attachments")
    @Operation(summary = "Get all attachments for an incident")
    public ResponseEntity<ApiResponse<List<AttachmentDTO>>> getIncidentAttachments(@PathVariable Long incidentId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getIncidentAttachments(incidentId)));
    }

    @GetMapping("/attachments/{id}/download")
    @Operation(summary = "Download attachment file by id")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentEntity(id);
        Resource resource = attachmentService.loadFileAsResource(attachment);

        String contentType = attachment.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
