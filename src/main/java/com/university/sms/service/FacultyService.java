package com.university.sms.service;

import java.util.List;
import java.util.Objects; // Added import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.sms.entity.Faculty;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import com.university.sms.repository.DepartmentRepository;
import com.university.sms.repository.FacultyRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditService auditService;

    public Faculty createFaculty(Faculty faculty) {
        Objects.requireNonNull(faculty, "Faculty object cannot be null");
        log.info("Creating faculty: {}", faculty.getEmail());
        
        if (facultyRepository.findByEmail(faculty.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists: " + faculty.getEmail());
        }

        Faculty savedFaculty = facultyRepository.save(faculty);
        auditService.logAction("Faculty", savedFaculty.getFacultyId(), "CREATE", null, savedFaculty.toString());
        return savedFaculty;
    }

    public Faculty updateFaculty(Long id, Faculty facultyDetails) {
        Objects.requireNonNull(id, "Faculty ID cannot be null");
        Objects.requireNonNull(facultyDetails, "Faculty details cannot be null");
        log.info("Updating faculty: {}", id);
        
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        String oldValues = faculty.toString();
        
        if (facultyDetails.getFirstName() != null) {
            faculty.setFirstName(facultyDetails.getFirstName());
        }
        if (facultyDetails.getLastName() != null) {
            faculty.setLastName(facultyDetails.getLastName());
        }
        if (facultyDetails.getPhoneNumber() != null) {
            faculty.setPhoneNumber(facultyDetails.getPhoneNumber());
        }
        if (facultyDetails.getSpecialization() != null) {
            faculty.setSpecialization(facultyDetails.getSpecialization());
        }
        if (facultyDetails.getStatus() != null) {
            faculty.setStatus(facultyDetails.getStatus());
        }

        Faculty updatedFaculty = facultyRepository.save(faculty);
        auditService.logAction("Faculty", id, "UPDATE", oldValues, updatedFaculty.toString());
        return updatedFaculty;
    }

    public void deleteFaculty(Long id) {
        Objects.requireNonNull(id, "Faculty ID cannot be null");
        log.info("Deleting faculty: {}", id);
        
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        auditService.logAction("Faculty", id, "DELETE", faculty.toString(), null);
        facultyRepository.deleteById(id);
    }

    public Faculty getFacultyById(Long id) {
        Objects.requireNonNull(id, "Faculty ID cannot be null");
        log.info("Fetching faculty: {}", id);
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }

    public Page<Faculty> getAllFaculty(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable object cannot be null");
        log.info("Fetching all faculty");
        return facultyRepository.findAll(pageable);
    }

    public List<Faculty> getFacultyByDepartment(Long departmentId) {
        Objects.requireNonNull(departmentId, "Department ID cannot be null");
        log.info("Fetching faculty for department: {}", departmentId);
        return facultyRepository.findByDepartmentId(departmentId);
    }

    public List<Faculty> getFacultyByStatus(Faculty.FacultyStatus status) {
        Objects.requireNonNull(status, "Status cannot be null");
        log.info("Fetching faculty with status: {}", status);
        return facultyRepository.findByStatus(status);
    }

    public Faculty getFacultyByEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        log.info("Fetching faculty by email: {}", email);
        return facultyRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with email: " + email));
    }
}
