package com.university.sms.service;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.sms.entity.Department;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import com.university.sms.repository.DepartmentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AuditService auditService;

    public Department createDepartment(Department department) {
        Objects.requireNonNull(department, "Department object cannot be null");
        Objects.requireNonNull(department.getDepartmentCode(), "Department code cannot be null");
        
        log.info("Creating department: {}", department.getDepartmentCode());

        if (departmentRepository.findByDepartmentCode(department.getDepartmentCode()).isPresent()) {
            throw new ValidationException("Department code already exists: " + department.getDepartmentCode());
        }

        Department savedDept = departmentRepository.save(department);
        
        // Use Objects.requireNonNull to resolve the null type warning for the generated ID
        auditService.logAction(
            "Department", 
            Objects.requireNonNull(savedDept.getDepartmentId(), "Saved department ID cannot be null"), 
            "CREATE", 
            null, 
            savedDept.toString()
        );
        
        return savedDept;
    }

    public Department updateDepartment(Long id, Department deptDetails) {
        Objects.requireNonNull(id, "Department ID cannot be null");
        Objects.requireNonNull(deptDetails, "Department details cannot be null");
        
        log.info("Updating department: {}", id);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        String oldValues = dept.toString();

        if (deptDetails.getDepartmentName() != null) {
            dept.setDepartmentName(deptDetails.getDepartmentName());
        }
        if (deptDetails.getDescription() != null) {
            dept.setDescription(deptDetails.getDescription());
        }
        if (deptDetails.getBuilding() != null) {
            dept.setBuilding(deptDetails.getBuilding());
        }

        Department updatedDept = departmentRepository.save(dept);
        auditService.logAction("Department", id, "UPDATE", oldValues, updatedDept.toString());
        return updatedDept;
    }

    public void deleteDepartment(Long id) {
        Objects.requireNonNull(id, "Department ID cannot be null");
        log.info("Deleting department: {}", id);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        auditService.logAction("Department", id, "DELETE", dept.toString(), null);
        departmentRepository.deleteById(id);
    }

    public Department getDepartmentById(Long id) {
        Objects.requireNonNull(id, "Department ID cannot be null");
        log.info("Fetching department: {}", id);
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public Page<Department> getAllDepartments(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable object cannot be null");
        log.info("Fetching all departments");
        return departmentRepository.findAll(pageable);
    }

    public Department getDepartmentByCode(String departmentCode) {
        Objects.requireNonNull(departmentCode, "Department code cannot be null");
        log.info("Fetching department by code: {}", departmentCode);
        return departmentRepository.findByDepartmentCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + departmentCode));
    }
}
