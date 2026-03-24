package com.university.sms.service;

import com.university.sms.entity.Course;
import com.university.sms.entity.Department;
import com.university.sms.repository.CourseRepository;
import com.university.sms.repository.DepartmentRepository;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditService auditService;

    public Course createCourse(Course course) {
        log.info("Creating course: {}", course.getCourseCode());
        
        if (courseRepository.findByCourseCode(course.getCourseCode()).isPresent()) {
            throw new ValidationException("Course code already exists: " + course.getCourseCode());
        }

        if (course.getCredits() == null || course.getCredits() <= 0) {
            throw new ValidationException("Credits must be greater than 0");
        }

        course.setCreatedDate(LocalDateTime.now());
        course.setUpdatedDate(LocalDateTime.now());
        course.setCurrentEnrollment(0);

        Course savedCourse = courseRepository.save(course);
        auditService.logAction("Course", savedCourse.getCourseId(), "CREATE", null, savedCourse.toString());
        return savedCourse;
    }

    public Course updateCourse(Long id, Course courseDetails) {
        log.info("Updating course: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        String oldValues = course.toString();
        
        if (courseDetails.getCourseName() != null) {
            course.setCourseName(courseDetails.getCourseName());
        }
        if (courseDetails.getDescription() != null) {
            course.setDescription(courseDetails.getDescription());
        }
        if (courseDetails.getCapacity() != null) {
            course.setCapacity(courseDetails.getCapacity());
        }
        if (courseDetails.getCredits() != null) {
            course.setCredits(courseDetails.getCredits());
        }

        course.setUpdatedDate(LocalDateTime.now());
        Course updatedCourse = courseRepository.save(course);
        auditService.logAction("Course", id, "UPDATE", oldValues, updatedCourse.toString());
        return updatedCourse;
    }

    public void deleteCourse(Long id) {
        log.info("Deleting course: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        auditService.logAction("Course", id, "DELETE", course.toString(), null);
        courseRepository.deleteById(id);
    }

    public Course getCourseById(Long id) {
        log.info("Fetching course: {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    public Page<Course> getAllCourses(Pageable pageable) {
        log.info("Fetching all courses");
        return courseRepository.findAll(pageable);
    }

    public List<Course> getAvailableCourses() {
        log.info("Fetching available courses");
        return courseRepository.findAvailableCourses();
    }

    public List<Course> getCoursesByDepartment(Long departmentId) {
        log.info("Fetching courses for department: {}", departmentId);
        return courseRepository.findByDepartmentId(departmentId);
    }

    public Course getByCourseCode(String courseCode) {
        log.info("Fetching course by code: {}", courseCode);
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }
}