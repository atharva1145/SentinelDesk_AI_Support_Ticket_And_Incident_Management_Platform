package com.sentineldesk.history;

import com.sentineldesk.common.response.PagedResponse;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional
    public void logAsync(Long userId, String action, String entityType, String entityId, String details, String ipAddress) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        AuditLog log = new AuditLog(user, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logSync(Long userId, String action, String entityType, String entityId, String details, String ipAddress) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        AuditLog log = new AuditLog(user, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDTO> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findAllWithUser(pageable);

        List<AuditLogDTO> dtoList = logPage.getContent().stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtoList,
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements(),
                logPage.getTotalPages(),
                logPage.isLast()
        );
    }
}
