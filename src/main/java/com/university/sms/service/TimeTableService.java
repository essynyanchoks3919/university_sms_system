package com.university.sms.service;

import com.university.sms.entity.*;
import com.university.sms.repository.*;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class TimeTableService {

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AuditService auditService;

    public TimeTable createTimeTableEntry(TimeTable timeTable) {
        log.info("Creating timetable entry for course offering: {}", timeTable.getCourseOffering().getCourseOfferingId());
        
        // Check for room conflicts
        List<TimeTable> conflicts = timeTableRepository.findConflictingTimeSlots(
                timeTable.getRoom().getRoomId(),
                timeTable.getDayOfWeek(),
                timeTable.getStartTime(),
                timeTable.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new ValidationException("Room is already booked during this time slot");
        }

        // Check faculty availability
        List<TimeTable> facultyConflicts = timeTableRepository.findByFacultyId(timeTable.getFaculty().getFacultyId());
        for (TimeTable existing : facultyConflicts) {
            if (existing.getDayOfWeek() == timeTable.getDayOfWeek() &&
                isTimeOverlap(existing.getStartTime(), existing.getEndTime(), 
                             timeTable.getStartTime(), timeTable.getEndTime())) {
                throw new ValidationException("Faculty has conflicting schedule");
            }
        }

        TimeTable savedTimeTable = timeTableRepository.save(timeTable);
        auditService.logAction("TimeTable", savedTimeTable.getTimeTableId(), "CREATE", null, savedTimeTable.toString());
        return savedTimeTable;
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public TimeTable updateTimeTable(Long id, TimeTable timeTableDetails) {
        log.info("Updating timetable entry: {}", id);
        
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));

        String oldValues = timeTable.toString();
        
        if (timeTableDetails.getStartTime() != null) {
            timeTable.setStartTime(timeTableDetails.getStartTime());
        }
        if (timeTableDetails.getEndTime() != null) {
            timeTable.setEndTime(timeTableDetails.getEndTime());
        }
        if (timeTableDetails.getDayOfWeek() != null) {
            timeTable.setDayOfWeek(timeTableDetails.getDayOfWeek());
        }

        TimeTable updatedTimeTable = timeTableRepository.save(timeTable);
        auditService.logAction("TimeTable", id, "UPDATE", oldValues, updatedTimeTable.toString());
        return updatedTimeTable;
    }

    public void deleteTimeTable(Long id) {
        log.info("Deleting timetable entry: {}", id);
        
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));

        auditService.logAction("TimeTable", id, "DELETE", timeTable.toString(), null);
        timeTableRepository.deleteById(id);
    }

    public List<TimeTable> getCourseTimeTable(Long courseOfferingId) {
        log.info("Fetching timetable for course offering: {}", courseOfferingId);
        return timeTableRepository.findByCourseOfferingId(courseOfferingId);
    }

    public List<TimeTable> getFacultySchedule(Long facultyId) {
        log.info("Fetching schedule for faculty: {}", facultyId);
        return timeTableRepository.findByFacultyId(facultyId);
    }

    public List<TimeTable> getRoomSchedule(Long roomId) {
        log.info("Fetching schedule for room: {}", roomId);
        return timeTableRepository.findByRoomId(roomId);
    }

    public TimeTable getTimeTableById(Long id) {
        log.info("Fetching timetable entry: {}", id);
        return timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));
    }
}