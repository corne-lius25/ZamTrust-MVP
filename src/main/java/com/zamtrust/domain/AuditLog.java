package com.zamtrust.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private String action;              // LOGIN, UPLOAD, SIGN, VERIFY, ...
    private String resource;            // document id / verification id
    private String details;

    @Column(nullable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();

    private String ipAddress;
}