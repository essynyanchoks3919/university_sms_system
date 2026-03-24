package com.university.sms.service;

import com.university.sms.entity.Audit;
import com.university.sms.repository.AuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public void logAction(String entityType, Long entityId, String action, String oldValue, String newValue) {
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

    public List<Audit> getAuditTrail(String entityType, Long entityId) {
        log.info("Fetching audit trail for {} - {}", entityType, entityId);
        return auditRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<Audit> getAuditsByUser(String username) {
        log.info("Fetching audits modified by {}", username);
        return auditRepository.findByModifiedBy(username);
    }

    public List<Audit> getAuditsByAction(String action) {
        log.info("Fetching audits with action: {}", action);
        return auditRepository.findByAction(Audit.AuditAction.valueOf(action));
    }
}