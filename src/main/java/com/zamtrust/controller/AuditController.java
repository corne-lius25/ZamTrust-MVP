package com.zamtrust.controller;

import com.zamtrust.domain.AuditLog;
import com.zamtrust.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/resource/{resource}")
    public List<AuditLog> byResource(@PathVariable String resource) {
        return auditService.forResource(resource);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ORG_ADMIN')")
    public List<AuditLog> byUser(@PathVariable Long userId) {
        return auditService.forUser(userId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<AuditLog> all() {
        return auditService.all();
    }
}
