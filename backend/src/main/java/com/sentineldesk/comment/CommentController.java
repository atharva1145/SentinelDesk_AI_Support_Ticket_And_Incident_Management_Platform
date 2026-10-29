package com.sentineldesk.comment;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments & Notes", description = "Public conversations and private internal staff notes")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "Get comments for ticket (internal notes filtered for requesters)")
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsForTicket(ticketId)));
    }

    @PostMapping
    @Operation(summary = "Add a comment or internal note")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponseDTO created = commentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Comment added", created));
    }
}
