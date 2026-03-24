package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private FeeService feeService;

    @Autowired
    private AuditService auditService;

    public Enrollment enrollStudent(Long studentId, Long courseOfferingId) {
        log.info("Enrolling student {} in course offering {}", studentId, courseOfferingId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        CourseOffering courseOffering = courseOfferingRepository.findById(courseOfferingId)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found with id: " + courseOfferingId));

        if (courseOffering.getEnrolledCount() >= courseOffering.getCourse().getCapacity()) {
            throw new ValidationException("Course is full");
        }

        if (student.getStatus() != Student.StudentStatus.ACTIVE) {
            throw new ValidationException("Student status must be ACTIVE to enroll");
        }

        // Check for financial hold
        if (feeService.hasFinancialHold(studentId)) {
            throw new ValidationException("Student has outstanding fees - financial hold in place");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .courseOffering(courseOffering)
                .enrollmentDate(LocalDateTime.now())
                .status(Enrollment.EnrollmentStatus.ENROLLED)
                .attendancePercentage(0)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        // Update course offering enrolled count
        courseOffering.setEnrolledCount(courseOffering.getEnrolledCount() + 1);
        courseOfferingRepository.save(courseOffering);
        
        // Add course to student's enrollments
        student.getEnrollments().add(savedEnrollment);
        student.setTotalCreditsEnrolled(student.getTotalCreditsEnrolled() + courseOffering.getCourse().getCredits());
        studentRepository.save(student);

        auditService.logAction("Enrollment", savedEnrollment.getEnrollmentId(), "CREATE", null, savedEnrollment.toString());
        
        return savedEnrollment;
    }

    public Enrollment dropCourse(Long enrollmentId) {
        log.info("Dropping course for enrollment: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        
        CourseOffering courseOffering = enrollment.getCourseOffering();
        courseOffering.setEnrolledCount(courseOffering.getEnrolledCount() - 1);
        courseOfferingRepository.save(courseOffering);

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        auditService.logAction("Enrollment", enrollmentId, "UPDATE", enrollment.toString(), updatedEnrollment.toString());
        
        return updatedEnrollment;
    }

    public List<Enrollment> getStudentEnrollments(Long studentId) {
        log.info("Fetching enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> getCourseEnrollments(Long courseOfferingId) {
        log.info("Fetching enrollments for course offering: {}", courseOfferingId);
        return enrollmentRepository.findByCourseOfferingId(courseOfferingId);
    }

    public Enrollment getEnrollmentById(Long id) {
        log.info("Fetching enrollment: {}", id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }

    public void updateAttendance(Long enrollmentId, Integer attendancePercentage) {
        log.info("Updating attendance for enrollment: {} to {}%", enrollmentId, attendancePercentage);
        
        if (attendancePercentage < 0 || attendancePercentage > 100) {
            throw new ValidationException("Attendance percentage must be between 0 and 100");
        }

        Enrollment enrollment = getEnrollmentById(enrollmentId);
        enrollment.setAttendancePercentage(attendancePercentage);
        enrollmentRepository.save(enrollment);
    }
}