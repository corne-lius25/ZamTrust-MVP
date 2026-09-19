package com.zamtrust.service;

import com.zamtrust.domain.AuditLog;
import com.zamtrust.domain.User;
import com.zamtrust.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User user, String action, String resource, String details, String ip) {
        AuditLog entry = AuditLog.builder()
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : "anonymous")
                .action(action)
                .resource(resource)
                .details(details)
                .ipAddress(ip)
                .build();
        auditLogRepository.save(entry);
        log.info("[AUDIT] user={} action={} resource={} details={}",
                entry.getUsername(), action, resource, details);
    }

    public List<AuditLog> forResource(String resource) {
        return auditLogRepository.findByResourceOrderByTimestampDesc(resource);
    }

    public List<AuditLog> forUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> all() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}