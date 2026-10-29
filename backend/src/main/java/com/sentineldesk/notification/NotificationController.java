package com.sentineldesk.notification;

import com.sentineldesk.common.response.ApiResponse;
import com.sentineldesk.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app notifications for ticket updates and SLA escalations")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;

    public NotificationController(NotificationService notificationService,
                                  SseNotificationService sseNotificationService) {
        this.notificationService = notificationService;
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time live events and notifications via Server-Sent Events (SSE)")
    public SseEmitter subscribeStream() {
        Long userId = SecurityUtils.getCurrentUserId();
        return sseNotificationService.subscribe(userId);
    }

    @GetMapping
    @Operation(summary = "Get notifications for current user")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getCurrentUserNotifications()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count for badge display")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PatchMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
