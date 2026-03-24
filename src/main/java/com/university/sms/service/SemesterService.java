package com.university.sms.service;

import com.university.sms.entity.Semester;
import com.university.sms.repository.SemesterRepository;
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
public class SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private AuditService auditService;

    public Semester createSemester(Semester semester) {
        log.info("Creating semester: {}", semester.getSemesterCode());
        
        if (semesterRepository.findBySemesterCode(semester.getSemesterCode()).isPresent()) {
            throw new ValidationException("Semester code already exists: " + semester.getSemesterCode());
        }

        Semester savedSemester = semesterRepository.save(semester);
        auditService.logAction("Semester", savedSemester.getSemesterId(), "CREATE", null, savedSemester.toString());
        return savedSemester;
    }

    public Semester updateSemester(Long id, Semester semesterDetails) {
        log.info("Updating semester: {}", id);
        
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));

        String oldValues = semester.toString();
        
        if (semesterDetails.getSemesterName() != null) {
            semester.setSemesterName(semesterDetails.getSemesterName());
        }
        if (semesterDetails.getIsActive() != null) {
            semester.setIsActive(semesterDetails.getIsActive());
        }
        if (semesterDetails.getSemesterYear() != null) {
            semester.setSemesterYear(semesterDetails.getSemesterYear());
        }

        Semester updatedSemester = semesterRepository.save(semester);
        auditService.logAction("Semester", id, "UPDATE", oldValues, updatedSemester.toString());
        return updatedSemester;
    }

    public void deleteSemester(Long id) {
        log.info("Deleting semester: {}", id);
        
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));

        auditService.logAction("Semester", id, "DELETE", semester.toString(), null);
        semesterRepository.deleteById(id);
    }

    public Semester getSemesterById(Long id) {
        log.info("Fetching semester: {}", id);
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
    }

    public Page<Semester> getAllSemesters(Pageable pageable) {
        log.info("Fetching all semesters");
        return semesterRepository.findAll(pageable);
    }

    public Semester getActiveSemester() {
        log.info("Fetching active semester");
        return semesterRepository.findActiveSemester()
                .orElseThrow(() -> new ResourceNotFoundException("No active semester found"));
    }

    public List<Semester> getSemestersByYear(Integer year) {
        log.info("Fetching semesters for year: {}", year);
        return semesterRepository.findByYear(year);
    }

    public Semester getSemesterByCode(String semesterCode) {
        log.info("Fetching semester by code: {}", semesterCode);
        return semesterRepository.findBySemesterCode(semesterCode)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with code: " + semesterCode));
    }
}