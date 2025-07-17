package com.cozyhavenstay.repository;

import com.cozyhavenstay.model.Room;
import com.cozyhavenstay.model.Room.BedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Custom query method: Find rooms by hotel ID
    List<Room> findByHotelId(Long hotelId);

    // Custom query method: Find rooms by hotel and bed type
    List<Room> findByHotelIdAndBedType(Long hotelId, BedType bedType);

    // You'll need more complex queries for availability, but this is a start.
    // Availability will likely be handled in the Service layer by checking against Bookings.
}