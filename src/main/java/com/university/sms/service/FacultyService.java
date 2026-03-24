package com.university.sms.service;

import com.university.sms.entity.Faculty;
import com.university.sms.entity.Department;
import com.university.sms.repository.FacultyRepository;
import com.university.sms.repository.DepartmentRepository;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        log.info("Creating faculty: {}", faculty.getEmail());
        
        if (facultyRepository.findByEmail(faculty.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists: " + faculty.getEmail());
        }

        Faculty savedFaculty = facultyRepository.save(faculty);
        auditService.logAction("Faculty", savedFaculty.getFacultyId(), "CREATE", null, savedFaculty.toString());
        return savedFaculty;
    }

    public Faculty updateFaculty(Long id, Faculty facultyDetails) {
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
        log.info("Deleting faculty: {}", id);
        
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        auditService.logAction("Faculty", id, "DELETE", faculty.toString(), null);
        facultyRepository.deleteById(id);
    }

    public Faculty getFacultyById(Long id) {
        log.info("Fetching faculty: {}", id);
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }

    public Page<Faculty> getAllFaculty(Pageable pageable) {
        log.info("Fetching all faculty");
        return facultyRepository.findAll(pageable);
    }

    public List<Faculty> getFacultyByDepartment(Long departmentId) {
        log.info("Fetching faculty for department: {}", departmentId);
        return facultyRepository.findByDepartmentId(departmentId);
    }

    public List<Faculty> getFacultyByStatus(Faculty.FacultyStatus status) {
        log.info("Fetching faculty with status: {}", status);
        return facultyRepository.findByStatus(status);
    }

    public Faculty getFacultyByEmail(String email) {
        log.info("Fetching faculty by email: {}", email);
        return facultyRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with email: " + email));
    }
}