package com.university.sms.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects; // Added for validation

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.sms.entity.TimeTable;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import com.university.sms.repository.CourseOfferingRepository;
import com.university.sms.repository.FacultyRepository;
import com.university.sms.repository.RoomRepository;
import com.university.sms.repository.TimeTableRepository;

import lombok.extern.slf4j.Slf4j;

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
        // Validate main object
        Objects.requireNonNull(timeTable, "TimeTable input cannot be null");
        
        // Wrap nested objects to satisfy strict type-checking
        var courseOffering = Objects.requireNonNull(timeTable.getCourseOffering(), "Course Offering is required");
        var room = Objects.requireNonNull(timeTable.getRoom(), "Room is required");
        var faculty = Objects.requireNonNull(timeTable.getFaculty(), "Faculty is required");
        var dayOfWeek = Objects.requireNonNull(timeTable.getDayOfWeek(), "Day of week is required");
        var startTime = Objects.requireNonNull(timeTable.getStartTime(), "Start time is required");
        var endTime = Objects.requireNonNull(timeTable.getEndTime(), "End time is required");

        log.info("Creating timetable entry for course offering: {}", courseOffering.getCourseOfferingId());
        
        // Use the validated local variables for repository calls
        List<TimeTable> conflicts = timeTableRepository.findConflictingTimeSlots(
                room.getRoomId(),
                dayOfWeek,
                startTime,
                endTime
        );

        if (!conflicts.isEmpty()) {
            throw new ValidationException("Room is already booked during this time slot");
        }

        // Check faculty availability using validated faculty object
        List<TimeTable> facultyConflicts = timeTableRepository.findByFacultyId(faculty.getFacultyId());
        for (TimeTable existing : facultyConflicts) {
            if (existing.getDayOfWeek() == dayOfWeek &&
                isTimeOverlap(existing.getStartTime(), existing.getEndTime(), 
                             startTime, endTime)) {
                throw new ValidationException("Faculty has conflicting schedule");
            }
        }

        TimeTable savedTimeTable = timeTableRepository.save(timeTable);
        
        // Ensure ID is non-null for the audit service
        Long savedId = Objects.requireNonNull(savedTimeTable.getTimeTableId(), "Saved ID cannot be null");
        auditService.logAction("TimeTable", savedId, "CREATE", null, savedTimeTable.toString());
        
        return savedTimeTable;
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Validating utility method parameters
        Objects.requireNonNull(start1);
        Objects.requireNonNull(end1);
        Objects.requireNonNull(start2);
        Objects.requireNonNull(end2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public TimeTable updateTimeTable(Long id, TimeTable timeTableDetails) {
        // Validate parameters
        Objects.requireNonNull(id, "ID is required for update");
        Objects.requireNonNull(timeTableDetails, "Update details cannot be null");

        log.info("Updating timetable entry: {}", id);
        
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));

        String oldValues = timeTable.toString();
        
        // Conditional updates
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
        Objects.requireNonNull(id, "ID is required for deletion");
        log.info("Deleting timetable entry: {}", id);
        
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));

        auditService.logAction("TimeTable", id, "DELETE", timeTable.toString(), null);
        timeTableRepository.deleteById(id);
    }

    public List<TimeTable> getCourseTimeTable(Long courseOfferingId) {
        Objects.requireNonNull(courseOfferingId, "Course Offering ID is required");
        log.info("Fetching timetable for course offering: {}", courseOfferingId);
        return timeTableRepository.findByCourseOfferingId(courseOfferingId);
    }

    public List<TimeTable> getFacultySchedule(Long facultyId) {
        Objects.requireNonNull(facultyId, "Faculty ID is required");
        log.info("Fetching schedule for faculty: {}", facultyId);
        return timeTableRepository.findByFacultyId(facultyId);
    }

    public List<TimeTable> getRoomSchedule(Long roomId) {
        Objects.requireNonNull(roomId, "Room ID is required");
        log.info("Fetching schedule for room: {}", roomId);
        return timeTableRepository.findByRoomId(roomId);
    }

    public TimeTable getTimeTableById(Long id) {
        Objects.requireNonNull(id, "ID is required to fetch record");
        log.info("Fetching timetable entry: {}", id);
        return timeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable entry not found with id: " + id));
    }
}
