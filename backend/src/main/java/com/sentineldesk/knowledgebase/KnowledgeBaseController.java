package com.sentineldesk.knowledgebase;

import com.sentineldesk.common.response.ApiResponse;
import com.sentineldesk.common.response.PagedResponse;
import com.sentineldesk.ticket.TicketCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge-base")
@Tag(name = "Knowledge Base", description = "IT runbooks, troubleshooting playbooks, and resolution guides")
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    public KnowledgeBaseController(KnowledgeBaseService kbService) {
        this.kbService = kbService;
    }

    @GetMapping
    @Operation(summary = "Search knowledge base articles and runbooks")
    public ResponseEntity<ApiResponse<PagedResponse<KnowledgeArticleDTO>>> searchArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(kbService.searchArticles(query, category, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full knowledge base article by ID (increments view count)")
    public ResponseEntity<ApiResponse<KnowledgeArticleDTO>> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(kbService.getArticleById(id)));
    }

    @GetMapping("/suggest")
    @Operation(summary = "Get smart recommended runbooks and solutions for a ticket")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleDTO>>> suggestArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TicketCategory category) {
        return ResponseEntity.ok(ApiResponse.success(kbService.suggestArticles(query, category)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Create a new knowledge base article or runbook")
    public ResponseEntity<ApiResponse<KnowledgeArticleDTO>> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        KnowledgeArticleDTO dto = kbService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", dto));
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Vote an article as helpful")
    public ResponseEntity<ApiResponse<KnowledgeArticleDTO>> voteHelpful(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vote recorded", kbService.voteHelpful(id)));
    }
}
