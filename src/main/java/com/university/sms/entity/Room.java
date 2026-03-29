package com.university.sms.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(unique = true, nullable = false, length = 50)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomType roomType = RoomType.CLASSROOM;

    private String building;
    private Integer floor;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean hasProjector = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean hasAc = false;

    public enum RoomType {
        CLASSROOM, LAB, SEMINAR, AUDITORIUM, LIBRARY
    }
}