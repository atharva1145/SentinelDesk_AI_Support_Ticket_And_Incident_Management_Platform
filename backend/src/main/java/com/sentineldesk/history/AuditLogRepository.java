package com.sentineldesk.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query(value = "SELECT a FROM AuditLog a LEFT JOIN FETCH a.user ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a")
    Page<AuditLog> findAllWithUser(Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
