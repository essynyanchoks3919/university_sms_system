package com.university.sms.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    @Builder.Default
    private Double examScore = 0.0;

    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    @Builder.Default
    private Double assignmentScore = 0.0;

    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    @Builder.Default
    private Double projectScore = 0.0;

    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    @Builder.Default
    private Double participationScore = 0.0;

    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    @Builder.Default
    private Double finalGrade = 0.0;

    private String letterGrade;

    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0.0")
    @Builder.Default
    private Double gradePoint = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    private String remarks;
}