package com.university.sms.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.sms.entity.CourseOffering;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.repository.CourseOfferingRepository;
import com.university.sms.repository.CourseRepository;
import com.university.sms.repository.FacultyRepository;
import com.university.sms.repository.SemesterRepository;

import lombok.extern.slf4j.Slf4j;

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

    @SuppressWarnings("null") // Suppresses persistent IDE-specific null safety markers
    public CourseOffering createCourseOffering(CourseOffering courseOffering) {
        log.info("Validating and creating course offering");

        if (courseOffering.getCourse() != null) {
            courseRepository.findById(courseOffering.getCourse().getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        }

        if (courseOffering.getFaculty() != null) {
            facultyRepository.findById(courseOffering.getFaculty().getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        }

        if (courseOffering.getSemester() != null) {
            semesterRepository.findById(courseOffering.getSemester().getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
        }
        
        courseOffering.setEnrolledCount(0);
        CourseOffering savedOffering = courseOfferingRepository.save(courseOffering);
        
        auditService.logAction("CourseOffering", Objects.requireNonNull(savedOffering.getCourseOfferingId()), "CREATE", null, savedOffering.toString());
        return savedOffering;
    }

    @SuppressWarnings("null")
    public CourseOffering updateCourseOffering(Long id, CourseOffering offeringDetails) {
        log.info("Updating course offering: {}", id);
        
        CourseOffering offering = courseOfferingRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        String oldValues = offering.toString();
        
        if (offeringDetails.getFaculty() != null) {
            facultyRepository.findById(offeringDetails.getFaculty().getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
            offering.setFaculty(offeringDetails.getFaculty());
        }

        CourseOffering updatedOffering = courseOfferingRepository.save(offering);
        auditService.logAction("CourseOffering", id, "UPDATE", oldValues, updatedOffering.toString());
        return updatedOffering;
    }
        
    public void deleteCourseOffering(Long id) {
        log.info("Deleting course offering: {}", id);
        
        CourseOffering offering = courseOfferingRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        auditService.logAction("CourseOffering", id, "DELETE", offering.toString(), null);
        courseOfferingRepository.deleteById(id);
    }

    public CourseOffering getCourseOfferingById(Long id) {
        log.info("Fetching course offering: {}", id);
        return courseOfferingRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));
    }

        public List<CourseOffering> getCourseOfferingsBySemester(Long semesterId) {
        log.info("Fetching course offerings for semester: {}", semesterId);
        return courseOfferingRepository.findBySemesterId(Objects.requireNonNull(semesterId));
    }

    public List<CourseOffering> getCourseOfferingsByFaculty(Long facultyId) {
        log.info("Fetching course offerings for faculty: {}", facultyId);
        return courseOfferingRepository.findByFacultyId(Objects.requireNonNull(facultyId));
    }

    public List<CourseOffering> getCourseOfferingsByCourse(Long courseId) {
        log.info("Fetching course offerings for course: {}", courseId);
        return courseOfferingRepository.findByCourseId(Objects.requireNonNull(courseId));
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
