package com.sentineldesk.ai;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI System", description = "AI provider health, configuration test, and triage utilities")
public class AiSystemController {

    private final AiProviderClient aiProviderClient;

    public AiSystemController(AiProviderClient aiProviderClient) {
        this.aiProviderClient = aiProviderClient;
    }

    @GetMapping("/status")
    @Operation(summary = "Check AI provider status and configuration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "provider", aiProviderClient.getProvider(),
                "model", aiProviderClient.getModel(),
                "configured", aiProviderClient.isEnabled(),
                "fallbackAvailable", true
        )));
    }

    @PostMapping("/test-connection")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Test AI provider connectivity and latency")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection() {
        Map<String, Object> result = aiProviderClient.testConnection();
        return ResponseEntity.ok(ApiResponse.success("AI connection test completed", result));
    }
}
