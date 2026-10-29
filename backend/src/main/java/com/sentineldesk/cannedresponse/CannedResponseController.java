package com.sentineldesk.cannedresponse;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/canned-responses")
@Tag(name = "Canned Responses", description = "Pre-approved reply templates for support agent productivity")
public class CannedResponseController {

    private final CannedResponseRepository repository;

    public CannedResponseController(CannedResponseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Get all canned reply templates")
    public ResponseEntity<ApiResponse<List<CannedResponseDTO>>> getAll() {
        List<CannedResponseDTO> list = repository.findAll().stream()
                .map(CannedResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
