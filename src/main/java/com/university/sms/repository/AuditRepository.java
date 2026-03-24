package com.university.sms.repository;

import com.university.sms.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    @Query("SELECT a FROM Audit a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.modifiedDate DESC")
    List<Audit> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM Audit a WHERE a.modifiedBy = :modifiedBy ORDER BY a.modifiedDate DESC")
    List<Audit> findByModifiedBy(@Param("modifiedBy") String modifiedBy);
    
    @Query("SELECT a FROM Audit a WHERE a.action = :action ORDER BY a.modifiedDate DESC")
    List<Audit> findByAction(@Param("action") Audit.AuditAction action);
    
    @Query("SELECT a FROM Audit a WHERE a.modifiedDate BETWEEN :startDate AND :endDate ORDER BY a.modifiedDate DESC")
    List<Audit> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}