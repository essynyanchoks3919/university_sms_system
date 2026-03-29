package com.university.sms.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, length = 100)
    private String modifiedBy;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime modifiedDate = LocalDateTime.now();

    private String description;
    private String ipAddress;

    public enum AuditAction {
        CREATE, UPDATE, DELETE, VIEW, EXPORT, LOGIN, LOGOUT
    }
}