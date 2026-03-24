package com.university.sms.repository;

import com.university.sms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
    @Query("SELECT r FROM Room r WHERE r.capacity >= :capacity")
    List<Room> findByCapacityGreaterThanOrEqual(@Param("capacity") Integer capacity);
    
    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType")
    List<Room> findByRoomType(@Param("roomType") Room.RoomType roomType);
    
    @Query("SELECT r FROM Room r WHERE r.building = :building")
    List<Room> findByBuilding(@Param("building") String building);
}