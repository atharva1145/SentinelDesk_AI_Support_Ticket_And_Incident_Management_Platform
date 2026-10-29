package com.sentineldesk.notification;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(User recipient, String title, String message, String link) {
        if (recipient == null) return null;
        Notification notification = new Notification(recipient, title, message, link);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getCurrentUserNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notif.getRecipient().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your notification");
        }

        notif.setRead(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
