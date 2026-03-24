package com.university.sms.service;

import com.university.sms.entity.Room;
import com.university.sms.repository.RoomRepository;
import com.university.sms.exception.ResourceNotFoundException;
import com.university.sms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AuditService auditService;

    public Room createRoom(Room room) {
        log.info("Creating room: {}", room.getRoomNumber());
        
        if (roomRepository.findByRoomNumber(room.getRoomNumber()).isPresent()) {
            throw new ValidationException("Room number already exists: " + room.getRoomNumber());
        }

        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ValidationException("Room capacity must be greater than 0");
        }

        Room savedRoom = roomRepository.save(room);
        auditService.logAction("Room", savedRoom.getRoomId(), "CREATE", null, savedRoom.toString());
        return savedRoom;
    }

    public Room updateRoom(Long id, Room roomDetails) {
        log.info("Updating room: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        String oldValues = room.toString();
        
        if (roomDetails.getCapacity() != null) {
            room.setCapacity(roomDetails.getCapacity());
        }
        if (roomDetails.getRoomType() != null) {
            room.setRoomType(roomDetails.getRoomType());
        }
        if (roomDetails.getBuilding() != null) {
            room.setBuilding(roomDetails.getBuilding());
        }
        if (roomDetails.getFloor() != null) {
            room.setFloor(roomDetails.getFloor());
        }
        if (roomDetails.getHasProjector() != null) {
            room.setHasProjector(roomDetails.getHasProjector());
        }
        if (roomDetails.getHasAc() != null) {
            room.setHasAc(roomDetails.getHasAc());
        }

        Room updatedRoom = roomRepository.save(room);
        auditService.logAction("Room", id, "UPDATE", oldValues, updatedRoom.toString());
        return updatedRoom;
    }

    public void deleteRoom(Long id) {
        log.info("Deleting room: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        auditService.logAction("Room", id, "DELETE", room.toString(), null);
        roomRepository.deleteById(id);
    }

    public Room getRoomById(Long id) {
        log.info("Fetching room: {}", id);
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public Page<Room> getAllRooms(Pageable pageable) {
        log.info("Fetching all rooms");
        return roomRepository.findAll(pageable);
    }

    public List<Room> getRoomsByCapacity(Integer capacity) {
        log.info("Fetching rooms with capacity >= {}", capacity);
        return roomRepository.findByCapacityGreaterThanOrEqual(capacity);
    }

    public List<Room> getRoomsByType(Room.RoomType roomType) {
        log.info("Fetching rooms of type: {}", roomType);
        return roomRepository.findByRoomType(roomType);
    }

    public List<Room> getRoomsByBuilding(String building) {
        log.info("Fetching rooms in building: {}", building);
        return roomRepository.findByBuilding(building);
    }

    public Room getRoomByNumber(String roomNumber) {
        log.info("Fetching room by number: {}", roomNumber);
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with number: " + roomNumber));
    }
}