package com.university.sms.repository;

import com.university.sms.entity.Fee;
import com.university.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    
    @Query("SELECT f FROM Fee f WHERE f.student.studentId = :studentId")
    List<Fee> findByStudentId(@Param("studentId") Long studentId);
    
    List<Fee> findByStudent(Student student);
    
    @Query("SELECT f FROM Fee f WHERE f.status = :status AND f.student.studentId = :studentId")
    List<Fee> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") Fee.FeeStatus status);
    
    @Query("SELECT f FROM Fee f WHERE f.dueDate < :date AND f.status != :status")
    List<Fee> findByDueDateBefore(@Param("date") LocalDate date, @Param("status") Fee.FeeStatus status);
    
    List<Fee> findByDueDateBefore(LocalDate date);
    
    @Query("SELECT SUM(f.amount) FROM Fee f WHERE f.student.studentId = :studentId AND f.status = :status")
    Double getTotalFeeByStudentAndStatus(@Param("studentId") Long studentId, @Param("status") Fee.FeeStatus status);
}