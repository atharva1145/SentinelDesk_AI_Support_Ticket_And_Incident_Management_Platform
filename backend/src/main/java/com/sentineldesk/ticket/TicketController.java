package com.sentineldesk.ticket;

import com.sentineldesk.common.response.ApiResponse;
import com.sentineldesk.common.response.PagedResponse;
import com.sentineldesk.history.TicketAssignmentHistoryDTO;
import com.sentineldesk.history.TicketStatusHistoryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Management", description = "Ticket creation, lifecycle workflow, assignment, priority, and history")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Submit a new support ticket")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponseDTO created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ticket created successfully", created));
    }

    @GetMapping
    @Operation(summary = "Search, filter, and paginate tickets")
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponseDTO>>> searchTickets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PagedResponse<TicketResponseDTO> response = ticketService.searchTickets(
                query, status, priority, category, agentId, teamId, unassigned, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket details by id")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> getTicketDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetails(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket fields (title, description, category)")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> updateTicket(
            @PathVariable Long id,
            @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated", ticketService.updateTicket(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change ticket status along valid workflow lifecycle")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeRequest request) {
        TicketResponseDTO response = ticketService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", response));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Assign or reassign ticket to an agent or team")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> assignTicket(
            @PathVariable Long id,
            @RequestBody AssignTicketRequest request) {
        TicketResponseDTO response = ticketService.assignTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket assignment updated", response));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER')")
    @Operation(summary = "Change ticket priority with mandatory reason (Manager/Admin only)")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> changePriority(
            @PathVariable Long id,
            @Valid @RequestBody PriorityChangeRequest request) {
        TicketResponseDTO response = ticketService.changePriority(id, request);
        return ResponseEntity.ok(ApiResponse.success("Priority updated", response));
    }

    @GetMapping("/{id}/status-history")
    @Operation(summary = "Get audit history of ticket status transitions")
    public ResponseEntity<ApiResponse<List<TicketStatusHistoryDTO>>> getStatusHistory(@PathVariable Long id) {
        List<TicketStatusHistoryDTO> history = ticketService.getStatusHistory(id).stream()
                .map(TicketStatusHistoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{id}/assignment-history")
    @Operation(summary = "Get audit history of ticket assignments")
    public ResponseEntity<ApiResponse<List<TicketAssignmentHistoryDTO>>> getAssignmentHistory(@PathVariable Long id) {
        List<TicketAssignmentHistoryDTO> history = ticketService.getAssignmentHistory(id).stream()
                .map(TicketAssignmentHistoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/{id}/csat")
    @Operation(summary = "Submit Customer Satisfaction (CSAT) rating and feedback for a resolved ticket")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> submitCsat(
            @PathVariable Long id,
            @Valid @RequestBody CsatRequest request) {
        TicketResponseDTO response = ticketService.submitCsat(id, request);
        return ResponseEntity.ok(ApiResponse.success("Thank you for your rating! Ticket has been closed.", response));
    }
}
