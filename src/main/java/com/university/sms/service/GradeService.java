package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@Transactional
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AuditService auditService;

    public Grade recordGrade(Grade grade) {
        log.info("Recording grade for enrollment: {}", grade.getEnrollment().getEnrollmentId());
        
        if (grade.getExamScore() == null || grade.getExamScore() < 0 || grade.getExamScore() > 100) {
            throw new ValidationException("Exam score must be between 0 and 100");
        }

        calculateFinalGrade(grade);
        Grade savedGrade = gradeRepository.save(grade);
        
        auditService.logAction("Grade", savedGrade.getGradeId(), "CREATE", null, savedGrade.toString());
        
        return savedGrade;
    }

    private void calculateFinalGrade(Grade grade) {
        Double examWeight = 0.40;
        Double assignmentWeight = 0.30;
        Double projectWeight = 0.20;
        Double participationWeight = 0.10;

        Double finalGrade = (grade.getExamScore() != null ? grade.getExamScore() : 0) * examWeight
                + (grade.getAssignmentScore() != null ? grade.getAssignmentScore() : 0) * assignmentWeight
                + (grade.getProjectScore() != null ? grade.getProjectScore() : 0) * projectWeight
                + (grade.getParticipationScore() != null ? grade.getParticipationScore() : 0) * participationWeight;

        grade.setFinalGrade(finalGrade);
        grade.setLetterGrade(convertToLetterGrade(finalGrade));
        grade.setGradePoint(convertToGradePoint(finalGrade));
    }

    private String convertToLetterGrade(Double score) {
        if (score >= 90) return "A";
        else if (score >= 80) return "B";
        else if (score >= 70) return "C";
        else if (score >= 60) return "D";
        else return "F";
    }

    private Double convertToGradePoint(Double score) {
        if (score >= 90) return 4.0;
        else if (score >= 80) return 3.5;
        else if (score >= 70) return 3.0;
        else if (score >= 60) return 2.0;
        else return 0.0;
    }

    public Grade updateGrade(Long id, Grade gradeDetails) {
        log.info("Updating grade: {}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        String oldValues = grade.toString();
        
        if (gradeDetails.getExamScore() != null) {
            grade.setExamScore(gradeDetails.getExamScore());
        }
        if (gradeDetails.getAssignmentScore() != null) {
            grade.setAssignmentScore(gradeDetails.getAssignmentScore());
        }
        if (gradeDetails.getProjectScore() != null) {
            grade.setProjectScore(gradeDetails.getProjectScore());
        }
        if (gradeDetails.getParticipationScore() != null) {
            grade.setParticipationScore(gradeDetails.getParticipationScore());
        }

        calculateFinalGrade(grade);
        Grade updatedGrade = gradeRepository.save(grade);
        
        auditService.logAction("Grade", id, "UPDATE", oldValues, updatedGrade.toString());
        
        return updatedGrade;
    }

    public List<Grade> getGradesByStudent(Long studentId) {
        log.info("Fetching grades for student: {}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        return gradeRepository.findByStudent(student);
    }

    public List<Grade> getGradesByCourseOffering(Long courseOfferingId) {
        log.info("Fetching grades for course offering: {}", courseOfferingId);
        return gradeRepository.findByCourseOfferingId(courseOfferingId);
    }

    @Transactional
    public List<Grade> bulkUploadGrades(List<Grade> grades) {
        log.info("Bulk uploading {} grades", grades.size());
        
        List<Grade> savedGrades = new ArrayList<>();
        
        for (Grade grade : grades) {
            try {
                calculateFinalGrade(grade);
                savedGrades.add(gradeRepository.save(grade));
            } catch (Exception e) {
                log.warn("Failed to upload grade for enrollment: {}", grade.getEnrollment().getEnrollmentId(), e);
            }
        }
        
        return savedGrades;
    }

    public Map<String, Object> generateTranscript(Long studentId) {
        log.info("Generating transcript for student: {}", studentId);
        
        Student student = studentService.getStudentById(studentId);
        List<Grade> grades = getGradesByStudent(studentId);

        Map<String, Object> transcript = new LinkedHashMap<>();
        transcript.put("studentId", student.getStudentId());
        transcript.put("firstName", student.getFirstName());
        transcript.put("lastName", student.getLastName());
        transcript.put("email", student.getEmail());
        transcript.put("department", student.getDepartment() != null ? student.getDepartment().getDepartmentName() : "N/A");
        transcript.put("enrollmentDate", student.getEnrollmentDate());
        transcript.put("cgpa", student.getCgpa());

        List<Map<String, Object>> courseGrades = new ArrayList<>();
        for (Grade grade : grades) {
            Map<String, Object> courseGrade = new LinkedHashMap<>();
            courseGrade.put("courseCode", grade.getEnrollment().getCourseOffering().getCourse().getCourseCode());
            courseGrade.put("courseName", grade.getEnrollment().getCourseOffering().getCourse().getCourseName());
            courseGrade.put("credits", grade.getEnrollment().getCourseOffering().getCourse().getCredits());
            courseGrade.put("finalGrade", grade.getFinalGrade());
            courseGrade.put("letterGrade", grade.getLetterGrade());
            courseGrade.put("gradePoint", grade.getGradePoint());
            courseGrades.add(courseGrade);
        }

        transcript.put("courses", courseGrades);
        return transcript;
    }
}