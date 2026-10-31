package com.sentineldesk.incident;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incident Management", description = "Endpoints for managing outages, major incidents, and linked tickets")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "Get all major incidents")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getAllIncidents() {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getAllIncidents()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident details by id")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidentById(id)));
    }

    @GetMapping("/{id}/post-mortem")
    @Operation(summary = "Generate Executive Major Incident Post-Mortem (RCA Report)")
    public ResponseEntity<ApiResponse<IncidentPostMortemDTO>> getPostMortem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.generatePostMortem(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Declare and create new incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        IncidentDTO created = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Incident created", created));
    }

    @PostMapping("/{id}/link-tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Link multiple tickets to incident")
    public ResponseEntity<ApiResponse<Void>> linkTickets(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> payload) {
        incidentService.linkTickets(id, payload.get("ticketIds"));
        return ResponseEntity.ok(ApiResponse.success("Tickets linked to incident", null));
    }

    @DeleteMapping("/{id}/link-tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Unlink ticket from incident")
    public ResponseEntity<ApiResponse<Void>> unlinkTicket(
            @PathVariable Long id,
            @PathVariable Long ticketId) {
        incidentService.unlinkTicket(id, ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket unlinked", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Update incident status and root cause")
    public ResponseEntity<ApiResponse<IncidentDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        IncidentStatus status = IncidentStatus.valueOf(payload.get("status"));
        String rootCause = payload.get("rootCause");
        String resolutionSummary = payload.get("resolutionSummary");
        IncidentDTO updated = incidentService.updateIncidentStatus(id, status, rootCause, resolutionSummary);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }
}
