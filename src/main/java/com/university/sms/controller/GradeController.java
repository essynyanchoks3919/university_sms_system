package com.university.sms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.sms.entity.Grade;
import com.university.sms.service.GradeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/grades")
@Slf4j
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @PostMapping
    public ResponseEntity<Grade> recordGrade(@RequestBody Grade grade) {
        log.info("Recording grade");
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.recordGrade(grade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        log.info("Fetching grade: {}", id);
        return ResponseEntity.ok(gradeService.getGradeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grade> updateGrade(@PathVariable Long id, @RequestBody Grade gradeDetails) {
        log.info("Updating grade: {}", id);
        return ResponseEntity.ok(gradeService.updateGrade(id, gradeDetails));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(@PathVariable Long studentId) {
        log.info("Fetching grades for student: {}", studentId);
        return ResponseEntity.ok(gradeService.getGradesByStudent(studentId));
    }

    @GetMapping("/course-offering/{courseOfferingId}")
    public ResponseEntity<List<Grade>> getGradesByCourseOffering(@PathVariable Long courseOfferingId) {
        log.info("Fetching grades for course offering: {}", courseOfferingId);
        return ResponseEntity.ok(gradeService.getGradesByCourseOffering(courseOfferingId));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<List<Grade>> bulkUploadGrades(@RequestBody List<Grade> grades) {
        log.info("Bulk uploading grades");
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.bulkUploadGrades(grades));
    }

    @GetMapping("/student/{studentId}/transcript")
    public ResponseEntity<Map<String, Object>> generateTranscript(@PathVariable Long studentId) {
        log.info("Generating transcript for student: {}", studentId);
        return ResponseEntity.ok(gradeService.generateTranscript(studentId));
    }
}