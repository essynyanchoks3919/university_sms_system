package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AuditService auditService;

    public Student createStudent(Student student) {
        Objects.requireNonNull(student, "Student object cannot be null");
        String email = Objects.requireNonNull(student.getEmail(), "Student email is required");
        
        log.info("Creating new student: {}", email);
        
        if (studentRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email already exists: " + email);
        }

        student.setEnrollmentDate(LocalDate.now());
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setCgpa(0.0);
        student.setTotalCreditsCompleted(0);

        Student savedStudent = studentRepository.save(student);
        
        // Wrap the ID to satisfy the compiler for the audit log
        Long savedId = Objects.requireNonNull(savedStudent.getStudentId(), "Saved Student ID cannot be null");
        auditService.logAction("Student", savedId, "CREATE", null, savedStudent.toString());
        
        log.info("Student created successfully: {}", savedId);
        return savedStudent;
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Objects.requireNonNull(id, "Student ID is required for update");
        Objects.requireNonNull(studentDetails, "Update details cannot be null");
        
        log.info("Updating student: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        String oldValues = student.toString();
        
        if (studentDetails.getFirstName() != null) {
            student.setFirstName(studentDetails.getFirstName());
        }
        if (studentDetails.getLastName() != null) {
            student.setLastName(studentDetails.getLastName());
        }
        if (studentDetails.getPhoneNumber() != null) {
            student.setPhoneNumber(studentDetails.getPhoneNumber());
        }
        if (studentDetails.getAddress() != null) {
            student.setAddress(studentDetails.getAddress());
        }
        if (studentDetails.getStatus() != null) {
            student.setStatus(studentDetails.getStatus());
        }

        Student updatedStudent = studentRepository.save(student);
        auditService.logAction("Student", id, "UPDATE", oldValues, updatedStudent.toString());
        
        return updatedStudent;
    }

    public void deleteStudent(Long id) {
        Objects.requireNonNull(id, "ID is required for deletion");
        log.info("Deleting student: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        auditService.logAction("Student", id, "DELETE", student.toString(), null);
        studentRepository.deleteById(id);
    }

    public Page<Student> getAllStudents(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable parameter cannot be null");
        log.info("Fetching all students, page: {}", pageable.getPageNumber());
        return studentRepository.findAll(pageable);
    }

    public Student getStudentById(Long id) {
        Objects.requireNonNull(id, "Student ID is required");
        log.info("Fetching student: {}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public List<Student> searchStudents(String firstName, String lastName, String email) {
        log.info("Searching students with firstName: {}, lastName: {}, email: {}", firstName, lastName, email);
        
        if (email != null && !email.isEmpty()) {
            return studentRepository.findByEmail(email).stream().collect(Collectors.toList());
        }
        
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return studentRepository.findByFirstNameContainingAndLastNameContaining(firstName, lastName);
        }
        
        if (firstName != null && !firstName.isEmpty()) {
            return studentRepository.findByFirstNameContaining(firstName);
        }
        
        return new ArrayList<>();
    }

    public List<Enrollment> getStudentEnrollments(Long studentId) {
        Objects.requireNonNull(studentId, "Student ID is required");
        log.info("Fetching enrollments for student: {}", studentId);
        Student student = getStudentById(studentId);
        // Ensure enrollments list is not null before wrapping in ArrayList
        return new ArrayList<>(Objects.requireNonNull(student.getEnrollments(), "Enrollments list is null"));
    }

    public Double calculateCGPA(Long studentId) {
        Objects.requireNonNull(studentId, "Student ID is required for CGPA calculation");
        log.info("Calculating CGPA for student: {}", studentId);
        
        Student student = getStudentById(studentId);
        List<Grade> grades = gradeRepository.findByStudent(student);

        if (grades == null || grades.isEmpty()) {
            return 0.0;
        }

        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            // Safely navigate the deep object graph to avoid null warnings
            if (grade != null && grade.getGradePoint() != null && grade.getEnrollment() != null) {
                var courseOffering = grade.getEnrollment().getCourseOffering();
                if (courseOffering != null && courseOffering.getCourse() != null) {
                    Integer credits = courseOffering.getCourse().getCredits();
                    if (credits != null) {
                        totalPoints += grade.getGradePoint() * credits;
                        totalCredits += credits;
                    }
                }
            }
        }

        double cgpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        student.setCgpa(cgpa);
        student.setTotalCreditsCompleted(totalCredits);
        studentRepository.save(student);

        return cgpa;
    }

    @Transactional
    public List<Student> importStudentsBatch(List<Student> students) {
        Objects.requireNonNull(students, "Batch list cannot be null");
        log.info("Batch importing {} students", students.size());
        
        List<Student> savedStudents = new ArrayList<>();
        
        for (Student student : students) {
            if (student == null) continue;
            try {
                String email = student.getEmail();
                if (email != null && studentRepository.findByEmail(email).isEmpty()) {
                    student.setEnrollmentDate(LocalDate.now());
                    student.setStatus(Student.StudentStatus.ACTIVE);
                    savedStudents.add(studentRepository.save(student));
                }
            } catch (Exception e) {
                log.warn("Failed to import student with email: {}", student.getEmail(), e);
            }
        }
        
        log.info("Successfully imported {} students", savedStudents.size());
        return savedStudents;
    }

    public List<Student> getStudentsByDepartment(Long departmentId) {
        Objects.requireNonNull(departmentId, "Department ID is required");
        log.info("Fetching students for department: {}", departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        return new ArrayList<>(Objects.requireNonNull(department.getStudents(), "Student list is null"));
    }

    public Student changeStudentStatus(Long id, Student.StudentStatus status) {
        Objects.requireNonNull(id, "Student ID is required");
        Objects.requireNonNull(status, "Status cannot be null");
        log.info("Changing student status to {} for student: {}", status, id);
        
        Student student = getStudentById(id);
        student.setStatus(status);
        return studentRepository.save(student);
    }
}
