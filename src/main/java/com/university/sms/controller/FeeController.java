package com.university.sms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.sms.entity.Fee;
import com.university.sms.service.FeeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fees")
@Slf4j
public class FeeController {

    @Autowired
    private FeeService feeService;

    @PostMapping
    public ResponseEntity<Fee> createFee(@RequestBody Fee fee) {
        log.info("Creating fee");
        return ResponseEntity.status(HttpStatus.CREATED).body(feeService.createFee(fee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fee> getFeeById(@PathVariable Long id) {
        log.info("Fetching fee: {}", id);
        return ResponseEntity.ok(feeService.getFeeById(id));
    }

    @PostMapping("/{id}/record-payment")
    public ResponseEntity<Fee> recordPayment(@PathVariable Long id, @RequestParam Double amount) {
        log.info("Recording payment for fee: {}", id);
        return ResponseEntity.ok(feeService.recordPayment(id, amount));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Fee>> getFeesByStudent(@PathVariable Long studentId) {
        log.info("Fetching fees for student: {}", studentId);
        return ResponseEntity.ok(feeService.getFeesByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<List<Fee>> getPendingFees(@PathVariable Long studentId) {
        log.info("Fetching pending fees for student: {}", studentId);
        return ResponseEntity.ok(feeService.getPendingFees(studentId));
    }

    @GetMapping("/student/{studentId}/overdue")
    public ResponseEntity<List<Fee>> getOverdueFees(@PathVariable Long studentId) {
        log.info("Fetching overdue fees for student: {}", studentId);
        return ResponseEntity.ok(feeService.getOverdueFees(studentId));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<Map<String, Object>> generateInvoice(@PathVariable Long id) {
        log.info("Generating invoice for fee: {}", id);
        return ResponseEntity.ok(feeService.generateInvoice(id));
    }

    @GetMapping("/student/{studentId}/financial-hold")
    public ResponseEntity<Boolean> hasFinancialHold(@PathVariable Long studentId) {
        log.info("Checking financial hold for student: {}", studentId);
        return ResponseEntity.ok(feeService.hasFinancialHold(studentId));
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(@PathVariable Long studentId) {
        log.info("Fetching financial summary for student: {}", studentId);
        return ResponseEntity.ok(feeService.getFinancialSummary(studentId));
    }

    @PostMapping("/calculate-late-fees")
    public ResponseEntity<String> calculateLateFees() {
        log.info("Calculating late fees");
        feeService.calculateAndApplyLateFee();
        return ResponseEntity.ok("Late fees calculated and applied");
    }
}