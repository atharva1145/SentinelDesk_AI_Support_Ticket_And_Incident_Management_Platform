package com.sentineldesk.escalation;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/escalations")
@Tag(name = "Escalations", description = "Monitor, acknowledge, and resolve automated SLA breach events")
public class EscalationController {

    private final EscalationService escalationService;

    public EscalationController(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Get all SLA escalation events")
    public ResponseEntity<ApiResponse<List<EscalationEventDTO>>> getAllEscalations() {
        return ResponseEntity.ok(ApiResponse.success(escalationService.getAllEscalations()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Update escalation status (ACKNOWLEDGED or RESOLVED)")
    public ResponseEntity<ApiResponse<EscalationEventDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        EscalationStatus status = EscalationStatus.valueOf(payload.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated", escalationService.updateStatus(id, status)));
    }
}
