package com.university.sms.repository;

import com.university.sms.entity.Grade;
import com.university.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    @Query("SELECT g FROM Grade g WHERE g.student.studentId = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);
    
    List<Grade> findByStudent(Student student);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.courseOffering.courseOfferingId = :courseOfferingId")
    List<Grade> findByCourseOfferingId(@Param("courseOfferingId") Long courseOfferingId);
    
    @Query("SELECT AVG(g.finalGrade) FROM Grade g WHERE g.student.studentId = :studentId")
    Double getAverageGradeByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT g FROM Grade g WHERE g.letterGrade = :letterGrade ORDER BY g.finalGrade DESC")
    List<Grade> findByLetterGrade(@Param("letterGrade") String letterGrade);
}