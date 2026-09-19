package com.zamtrust.repository;

import com.zamtrust.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByResourceOrderByTimestampDesc(String resource);
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    List<AuditLog> findAllByOrderByTimestampDesc();
}