package com.sentineldesk.team;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Team Management", description = "Endpoints for managing support teams and agent assignments")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "List all support teams")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllTeams()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team details by id")
    public ResponseEntity<ApiResponse<TeamDTO>> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER')")
    @Operation(summary = "Create a new support team")
    public ResponseEntity<ApiResponse<TeamDTO>> createTeam(@RequestBody Map<String, String> payload) {
        TeamDTO created = teamService.createTeam(
                payload.get("name"),
                payload.get("description"),
                payload.get("categoryFocus")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Team created successfully", created));
    }

    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER')")
    @Operation(summary = "Add member to team")
    public ResponseEntity<ApiResponse<TeamMemberDTO>> addMember(
            @PathVariable Long teamId,
            @RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        boolean isLead = Boolean.parseBoolean(payload.getOrDefault("isTeamLead", "false").toString());
        TeamMemberDTO member = teamService.addMember(teamId, userId, isLead);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Member added", member));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER')")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }
}
