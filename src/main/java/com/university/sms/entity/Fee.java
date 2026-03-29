package com.university.sms.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeeStatus status = FeeStatus.PENDING;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0.0")
    @Builder.Default
    private Double fineAmount = 0.0;

    private String description;
    private String invoiceNumber;
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    public enum FeeStatus {
        PENDING, PAID, OVERDUE, PARTIALLY_PAID, WAIVED, CANCELLED
    }
}