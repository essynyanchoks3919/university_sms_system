package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class CourseOfferingService {

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private AuditService auditService;

    public CourseOffering createCourseOffering(CourseOffering courseOffering) {
        log.info("Creating course offering");
        
        courseOffering.setEnrolledCount(0);
        CourseOffering savedOffering = courseOfferingRepository.save(courseOffering);
        auditService.logAction("CourseOffering", savedOffering.getCourseOfferingId(), "CREATE", null, savedOffering.toString());
        return savedOffering;
    }

    public CourseOffering updateCourseOffering(Long id, CourseOffering offeringDetails) {
        log.info("Updating course offering: {}", id);
        
        CourseOffering offering = courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found with id: " + id));

        String oldValues = offering.toString();
        
        if (offeringDetails.getFaculty() != null) {
            offering.setFaculty(offeringDetails.getFaculty());
        }

        CourseOffering updatedOffering = courseOfferingRepository.save(offering);
        auditService.logAction("CourseOffering", id, "UPDATE", oldValues, updatedOffering.toString());
        return updatedOffering;
    }

    public void deleteCourseOffering(Long id) {
        log.info("Deleting course offering: {}", id);
        
        CourseOffering offering = courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found with id: " + id));

        auditService.logAction("CourseOffering", id, "DELETE", offering.toString(), null);
        courseOfferingRepository.deleteById(id);
    }

    public CourseOffering getCourseOfferingById(Long id) {
        log.info("Fetching course offering: {}", id);
        return courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found with id: " + id));
    }

    public List<CourseOffering> getCourseOfferingsBySemester(Long semesterId) {
        log.info("Fetching course offerings for semester: {}", semesterId);
        return courseOfferingRepository.findBySemesterId(semesterId);
    }

    public List<CourseOffering> getCourseOfferingsByFaculty(Long facultyId) {
        log.info("Fetching course offerings for faculty: {}", facultyId);
        return courseOfferingRepository.findByFacultyId(facultyId);
    }

    public List<CourseOffering> getCourseOfferingsByCourse(Long courseId) {
        log.info("Fetching course offerings for course: {}", courseId);
        return courseOfferingRepository.findByCourseId(courseId);
    }

    public List<CourseOffering> getCurrentSemesterOfferings() {
        log.info("Fetching current semester offerings");
        return courseOfferingRepository.findCurrentSemesterOfferings();
    }

    public List<CourseOffering> getAvailableCourseOfferings() {
        log.info("Fetching available course offerings");
        return courseOfferingRepository.findAvailableCourseOfferings();
    }
}