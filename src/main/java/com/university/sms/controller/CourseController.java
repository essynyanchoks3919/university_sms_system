package com.university.sms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.sms.entity.Course;
import com.university.sms.service.CourseService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/courses")
@Slf4j
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        log.info("Creating new course");
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(course));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        log.info("Fetching course: {}", id);
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Course>> getAllCourses(Pageable pageable) {
        log.info("Fetching all courses");
        return ResponseEntity.ok(courseService.getAllCourses(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        log.info("Updating course: {}", id);
        return ResponseEntity.ok(courseService.updateCourse(id, courseDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("Deleting course: {}", id);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<Course>> getAvailableCourses() {
        log.info("Fetching available courses");
        return ResponseEntity.ok(courseService.getAvailableCourses());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable Long departmentId) {
        log.info("Fetching courses for department: {}", departmentId);
        return ResponseEntity.ok(courseService.getCoursesByDepartment(departmentId));
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<Course> getCourseByCourseCode(@PathVariable String courseCode) {
        log.info("Fetching course by code: {}", courseCode);
        return ResponseEntity.ok(courseService.getByCourseCode(courseCode));
    }
}