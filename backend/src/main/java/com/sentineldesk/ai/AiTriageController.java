package com.sentineldesk.ai;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/ai-triage")
@Tag(name = "AI Ticket Triage", description = "Review, accept, or override AI ticket classification and diagnostics")
public class AiTriageController {

    private final AiTriageService aiTriageService;

    public AiTriageController(AiTriageService aiTriageService) {
        this.aiTriageService = aiTriageService;
    }

    @GetMapping
    @Operation(summary = "Get AI triage history for ticket")
    public ResponseEntity<ApiResponse<List<AiTriageResponseDTO>>> getSuggestions(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(aiTriageService.getSuggestionsForTicket(ticketId)));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest AI triage suggestion for ticket")
    public ResponseEntity<ApiResponse<AiTriageResponseDTO>> getLatestSuggestion(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(aiTriageService.getLatestSuggestionForTicket(ticketId)));
    }

    @PostMapping("/{suggestionId}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Accept AI recommendation")
    public ResponseEntity<ApiResponse<AiTriageResponseDTO>> acceptSuggestion(
            @PathVariable Long ticketId,
            @PathVariable Long suggestionId) {
        AiTriageResponseDTO result = aiTriageService.acceptSuggestion(ticketId, suggestionId);
        return ResponseEntity.ok(ApiResponse.success("AI suggestion accepted", result));
    }

    @PostMapping("/{suggestionId}/override")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Override AI recommendation with mandatory human reason")
    public ResponseEntity<ApiResponse<AiTriageResponseDTO>> overrideSuggestion(
            @PathVariable Long ticketId,
            @PathVariable Long suggestionId,
            @Valid @RequestBody OverrideAiSuggestionRequest request) {
        AiTriageResponseDTO result = aiTriageService.overrideSuggestion(ticketId, suggestionId, request);
        return ResponseEntity.ok(ApiResponse.success("AI suggestion overridden", result));
    }
}
