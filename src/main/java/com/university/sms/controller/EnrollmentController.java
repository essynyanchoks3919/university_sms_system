package com.university.sms.controller;

import com.university.sms.entity.Enrollment;
import com.university.sms.dto.EnrollmentDTO;
import com.university.sms.service.EnrollmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Slf4j
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<Enrollment> enrollStudent(@RequestParam Long studentId, @RequestParam Long courseOfferingId) {
        log.info("Enrolling student {} in course offering {}", studentId, courseOfferingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollStudent(studentId, courseOfferingId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Long id) {
        log.info("Fetching enrollment: {}", id);
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getStudentEnrollments(@PathVariable Long studentId) {
        log.info("Fetching enrollments for student: {}", studentId);
        return ResponseEntity.ok(enrollmentService.getStudentEnrollments(studentId));
    }

    @GetMapping("/course-offering/{courseOfferingId}")
    public ResponseEntity<List<Enrollment>> getCourseEnrollments(@PathVariable Long courseOfferingId) {
        log.info("Fetching enrollments for course offering: {}", courseOfferingId);
        return ResponseEntity.ok(enrollmentService.getCourseEnrollments(courseOfferingId));
    }

    @PostMapping("/{id}/drop")
    public ResponseEntity<Enrollment> dropCourse(@PathVariable Long id) {
        log.info("Dropping course for enrollment: {}", id);
        return ResponseEntity.ok(enrollmentService.dropCourse(id));
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<String> updateAttendance(@PathVariable Long id, @RequestParam Integer percentage) {
        log.info("Updating attendance for enrollment: {}", id);
        enrollmentService.updateAttendance(id, percentage);
        return ResponseEntity.ok("Attendance updated successfully");
    }
}