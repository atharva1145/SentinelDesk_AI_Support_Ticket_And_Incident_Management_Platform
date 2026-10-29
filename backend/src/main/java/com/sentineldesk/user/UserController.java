package com.sentineldesk.user;

import com.sentineldesk.common.response.ApiResponse;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.security.UserPrincipal;
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
@RequestMapping("/api/v1")
@Tag(name = "Authentication & Users", description = "Endpoints for user authentication, profile, and role administration")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Register new requester account")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = userService.registerRequester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", userDTO));
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserDTO userDTO = userService.getUserById(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/agents")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Get all support agents and managers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getStaffAndAgents() {
        return ResponseEntity.ok(ApiResponse.success(userService.getStaffAndAgents()));
    }

    @PatchMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        Role role = Role.valueOf(payload.get("role"));
        return ResponseEntity.ok(ApiResponse.success("Role updated", userService.updateUserRole(userId, role)));
    }

    @PatchMapping("/admin/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user active status (Admin only)")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> payload) {
        boolean active = payload.getOrDefault("active", true);
        return ResponseEntity.ok(ApiResponse.success("Status updated", userService.toggleUserStatus(userId, active)));
    }
}
