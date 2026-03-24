package com.university.sms.repository;

import com.university.sms.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {
    
    @Query("SELECT t FROM TimeTable t WHERE t.courseOffering.courseOfferingId = :courseOfferingId")
    List<TimeTable> findByCourseOfferingId(@Param("courseOfferingId") Long courseOfferingId);
    
    @Query("SELECT t FROM TimeTable t WHERE t.faculty.facultyId = :facultyId")
    List<TimeTable> findByFacultyId(@Param("facultyId") Long facultyId);
    
    @Query("SELECT t FROM TimeTable t WHERE t.room.roomId = :roomId")
    List<TimeTable> findByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT t FROM TimeTable t WHERE t.dayOfWeek = :dayOfWeek")
    List<TimeTable> findByDayOfWeek(@Param("dayOfWeek") TimeTable.DayOfWeek dayOfWeek);
    
    @Query("SELECT t FROM TimeTable t WHERE t.room.roomId = :roomId AND t.dayOfWeek = :dayOfWeek " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<TimeTable> findConflictingTimeSlots(@Param("roomId") Long roomId, 
                                             @Param("dayOfWeek") TimeTable.DayOfWeek dayOfWeek,
                                             @Param("startTime") LocalTime startTime, 
                                             @Param("endTime") LocalTime endTime);
}