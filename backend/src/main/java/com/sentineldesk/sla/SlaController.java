package com.sentineldesk.sla;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
@Tag(name = "SLA Policies & Tracking", description = "Service Level Agreement policies, deadlines, and real-time trackers")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/policies")
    @Operation(summary = "Get all configured SLA policies")
    public ResponseEntity<ApiResponse<List<SlaPolicyDTO>>> getAllPolicies() {
        return ResponseEntity.ok(ApiResponse.success(slaService.getAllPolicies()));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER')")
    @Operation(summary = "Update SLA policy target times (Manager/Admin only)")
    public ResponseEntity<ApiResponse<SlaPolicyDTO>> updatePolicy(
            @PathVariable Long id,
            @RequestBody SlaPolicyDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Policy updated", slaService.updatePolicy(id, dto)));
    }

    @GetMapping("/trackers/{ticketId}")
    @Operation(summary = "Get SLA tracker status and remaining time for ticket")
    public ResponseEntity<ApiResponse<SlaTrackerDTO>> getTicketSlaTracker(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(slaService.getTrackerForTicket(ticketId)));
    }
}
