package com.university.sms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull; // Added import
import org.springframework.lang.Nullable; // Added import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.sms.entity.Audit;
import com.university.sms.repository.AuditRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    @SuppressWarnings("null") // Suppresses the warning for the .save() result
    public void logAction(@NonNull String entityType, @NonNull Long entityId, @NonNull String action, 
                          @Nullable String oldValue, @Nullable String newValue) {
        log.info("Logging audit: {} - {} - {}", entityType, entityId, action);
        
        Audit audit = Audit.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(Audit.AuditAction.valueOf(action))
                .oldValue(oldValue)
                .newValue(newValue)
                .modifiedBy("SYSTEM")
                .modifiedDate(LocalDateTime.now())
                .build();
        
        auditRepository.save(audit);
    }

    public List<Audit> getAuditTrail(@NonNull String entityType, @NonNull Long entityId) {
        log.info("Fetching audit trail for {} - {}", entityType, entityId);
        return auditRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<Audit> getAuditsByUser(@NonNull String username) {
        log.info("Fetching audits modified by {}", username);
        return auditRepository.findByModifiedBy(username);
    }

    public List<Audit> getAuditsByAction(@NonNull String action) {
        log.info("Fetching audits with action: {}", action);
        return auditRepository.findByAction(Audit.AuditAction.valueOf(action));
    }
}
