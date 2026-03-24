package com.university.sms.controller;

import com.university.sms.entity.TimeTable;
import com.university.sms.dto.TimeTableDTO;
import com.university.sms.service.TimeTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
@Slf4j
public class TimeTableController {

    @Autowired
    private TimeTableService timeTableService;

    @PostMapping
    public ResponseEntity<TimeTable> createTimeTable(@RequestBody TimeTable timeTable) {
        log.info("Creating timetable entry");
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTableService.createTimeTableEntry(timeTable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeTable> getTimeTableById(@PathVariable Long id) {
        log.info("Fetching timetable: {}", id);
        return ResponseEntity.ok(timeTableService.getTimeTableById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeTable> updateTimeTable(@PathVariable Long id, @RequestBody TimeTable timeTableDetails) {
        log.info("Updating timetable: {}", id);
        return ResponseEntity.ok(timeTableService.updateTimeTable(id, timeTableDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeTable(@PathVariable Long id) {
        log.info("Deleting timetable: {}", id);
        timeTableService.deleteTimeTable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course-offering/{courseOfferingId}")
    public ResponseEntity<List<TimeTable>> getCourseTimeTable(@PathVariable Long courseOfferingId) {
        log.info("Fetching timetable for course offering: {}", courseOfferingId);
        return ResponseEntity.ok(timeTableService.getCourseTimeTable(courseOfferingId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<TimeTable>> getFacultySchedule(@PathVariable Long facultyId) {
        log.info("Fetching schedule for faculty: {}", facultyId);
        return ResponseEntity.ok(timeTableService.getFacultySchedule(facultyId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<TimeTable>> getRoomSchedule(@PathVariable Long roomId) {
        log.info("Fetching schedule for room: {}", roomId);
        return ResponseEntity.ok(timeTableService.getRoomSchedule(roomId));
    }
}