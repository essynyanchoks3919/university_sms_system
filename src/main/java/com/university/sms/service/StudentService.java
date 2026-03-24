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
        log.info("Creating new student: {}", student.getEmail());
        
        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists: " + student.getEmail());
        }

        student.setEnrollmentDate(LocalDate.now());
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setCgpa(0.0);
        student.setTotalCreditsCompleted(0);

        Student savedStudent = studentRepository.save(student);
        auditService.logAction("Student", savedStudent.getStudentId(), "CREATE", null, savedStudent.toString());
        
        log.info("Student created successfully: {}", savedStudent.getStudentId());
        return savedStudent;
    }

    public Student updateStudent(Long id, Student studentDetails) {
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
        log.info("Deleting student: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        auditService.logAction("Student", id, "DELETE", student.toString(), null);
        studentRepository.deleteById(id);
    }

    public Page<Student> getAllStudents(Pageable pageable) {
        log.info("Fetching all students, page: {}", pageable.getPageNumber());
        return studentRepository.findAll(pageable);
    }

    public Student getStudentById(Long id) {
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
        log.info("Fetching enrollments for student: {}", studentId);
        Student student = getStudentById(studentId);
        return new ArrayList<>(student.getEnrollments());
    }

    public Double calculateCGPA(Long studentId) {
        log.info("Calculating CGPA for student: {}", studentId);
        
        Student student = getStudentById(studentId);
        List<Grade> grades = gradeRepository.findByStudent(student);

        if (grades.isEmpty()) {
            return 0.0;
        }

        Double totalPoints = 0.0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            if (grade.getGradePoint() != null && grade.getEnrollment().getCourseOffering().getCourse().getCredits() != null) {
                totalPoints += grade.getGradePoint() * grade.getEnrollment().getCourseOffering().getCourse().getCredits();
                totalCredits += grade.getEnrollment().getCourseOffering().getCourse().getCredits();
            }
        }

        Double cgpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        student.setCgpa(cgpa);
        student.setTotalCreditsCompleted(totalCredits);
        studentRepository.save(student);

        return cgpa;
    }

    @Transactional
    public List<Student> importStudentsBatch(List<Student> students) {
        log.info("Batch importing {} students", students.size());
        
        List<Student> savedStudents = new ArrayList<>();
        
        for (Student student : students) {
            try {
                if (studentRepository.findByEmail(student.getEmail()).isEmpty()) {
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
        log.info("Fetching students for department: {}", departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        return new ArrayList<>(department.getStudents());
    }

    public Student changeStudentStatus(Long id, Student.StudentStatus status) {
        log.info("Changing student status to {} for student: {}", status, id);
        
        Student student = getStudentById(id);
        student.setStatus(status);
        return studentRepository.save(student);
    }
}