package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@Transactional
public class FeeService {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AuditService auditService;

    private static final Double LATE_FEE_PERCENTAGE = 0.05; // 5% fine

    public Fee createFee(Fee fee) {
        log.info("Creating fee for student: {}", fee.getStudent().getStudentId());
        
        if (fee.getAmount() == null || fee.getAmount() <= 0) {
            throw new ValidationException("Fee amount must be greater than 0");
        }

        fee.setStatus(Fee.FeeStatus.PENDING);
        fee.setFineAmount(0.0);
        fee.setCreatedDate(LocalDateTime.now());

        Fee savedFee = feeRepository.save(fee);
        auditService.logAction("Fee", savedFee.getFeeId(), "CREATE", null, savedFee.toString());
        
        return savedFee;
    }

    public Fee recordPayment(Long feeId, Double paymentAmount) {
        log.info("Recording payment of {} for fee: {}", paymentAmount, feeId);
        
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found with id: " + feeId));

        if (paymentAmount <= 0) {
            throw new ValidationException("Payment amount must be greater than 0");
        }

        Double totalDue = fee.getAmount() + (fee.getFineAmount() != null ? fee.getFineAmount() : 0);

        if (paymentAmount > totalDue) {
            throw new ValidationException("Payment amount exceeds total due");
        }

        if (paymentAmount >= totalDue) {
            fee.setStatus(Fee.FeeStatus.PAID);
            fee.setPaymentDate(LocalDate.now());
        } else {
            fee.setStatus(Fee.FeeStatus.PARTIALLY_PAID);
            fee.setAmount(fee.getAmount() - paymentAmount);
        }

        Fee updatedFee = feeRepository.save(fee);
        auditService.logAction("Fee", feeId, "UPDATE", fee.toString(), updatedFee.toString());
        
        return updatedFee;
    }

    @Transactional
    public void calculateAndApplyLateFee() {
        log.info("Calculating and applying late fees");
        
        List<Fee> overdueFees = feeRepository.findByDueDateBefore(LocalDate.now());

        for (Fee fee : overdueFees) {
            if (fee.getStatus() == Fee.FeeStatus.PENDING || fee.getStatus() == Fee.FeeStatus.PARTIALLY_PAID) {
                Double lateFee = fee.getAmount() * LATE_FEE_PERCENTAGE;
                fee.setFineAmount((fee.getFineAmount() != null ? fee.getFineAmount() : 0) + lateFee);
                fee.setStatus(Fee.FeeStatus.OVERDUE);
                feeRepository.save(fee);
            }
        }
    }

    public List<Fee> getFeesByStudent(Long studentId) {
        log.info("Fetching fees for student: {}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        return feeRepository.findByStudent(student);
    }

    public List<Fee> getPendingFees(Long studentId) {
        log.info("Fetching pending fees for student: {}", studentId);
        return feeRepository.findByStudentIdAndStatus(studentId, Fee.FeeStatus.PENDING);
    }

    public List<Fee> getOverdueFees(Long studentId) {
        log.info("Fetching overdue fees for student: {}", studentId);
        return feeRepository.findByStudentIdAndStatus(studentId, Fee.FeeStatus.OVERDUE);
    }

    public Map<String, Object> generateInvoice(Long feeId) {
        log.info("Generating invoice for fee: {}", feeId);
        
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found with id: " + feeId));

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", fee.getInvoiceNumber() != null ? fee.getInvoiceNumber() : "INV-" + fee.getFeeId());
        invoice.put("studentId", fee.getStudent().getStudentId());
        invoice.put("studentName", fee.getStudent().getFirstName() + " " + fee.getStudent().getLastName());
        invoice.put("email", fee.getStudent().getEmail());
        invoice.put("description", fee.getDescription());
        invoice.put("amount", fee.getAmount());
        invoice.put("fineAmount", fee.getFineAmount());
        invoice.put("totalDue", fee.getAmount() + (fee.getFineAmount() != null ? fee.getFineAmount() : 0));
        invoice.put("dueDate", fee.getDueDate());
        invoice.put("status", fee.getStatus());
        invoice.put("generatedDate", LocalDateTime.now());

        return invoice;
    }

    public Boolean hasFinancialHold(Long studentId) {
        log.info("Checking financial hold for student: {}", studentId);
        
        List<Fee> overdueFees = feeRepository.findByStudentIdAndStatus(studentId, Fee.FeeStatus.OVERDUE);
        return !overdueFees.isEmpty();
    }

    public Map<String, Object> getFinancialSummary(Long studentId) {
        log.info("Fetching financial summary for student: {}", studentId);
        
        List<Fee> fees = getFeesByStudent(studentId);

        Double totalPaid = fees.stream()
                .filter(f -> f.getStatus() == Fee.FeeStatus.PAID)
                .mapToDouble(Fee::getAmount)
                .sum();

        Double totalPending = fees.stream()
                .filter(f -> f.getStatus() == Fee.FeeStatus.PENDING)
                .mapToDouble(Fee::getAmount)
                .sum();

        Double totalOverdue = fees.stream()
                .filter(f -> f.getStatus() == Fee.FeeStatus.OVERDUE)
                .mapToDouble(f -> f.getAmount() + (f.getFineAmount() != null ? f.getFineAmount() : 0))
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalPaid", totalPaid);
        summary.put("totalPending", totalPending);
        summary.put("totalOverdue", totalOverdue);
        summary.put("hasFinancialHold", hasFinancialHold(studentId));

        return summary;
    }
}