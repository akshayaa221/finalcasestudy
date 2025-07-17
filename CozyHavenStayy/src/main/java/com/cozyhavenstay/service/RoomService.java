package com.cozyhavenstay.service;

import com.cozyhavenstay.model.Room;
import com.cozyhavenstay.model.Hotel;
import com.cozyhavenstay.repository.RoomRepository;
import com.cozyhavenstay.repository.HotelRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public Room addRoom(Long hotelId, Room room) {
        Hotel hotel = hotelRepository.findById(hotelId)
                                     .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + hotelId));
        room.setHotel(hotel); // Associate the room with the found hotel
        // Ensure 'available' is set if not provided by frontend (default true)
        if (room.getAvailable() == null) {
            room.setAvailable(true);
        }
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        return roomRepository.findById(id).map(room -> {
            room.setRoomNumber(roomDetails.getRoomNumber());
            room.setRoomSize(roomDetails.getRoomSize());
            room.setSize(roomDetails.getSize()); // Update 'size' field
            room.setBedType(roomDetails.getBedType());
            room.setMaxOccupancy(roomDetails.getMaxOccupancy());
            room.setMaxPeople(roomDetails.getMaxPeople()); // Update 'maxPeople' field
            room.setBaseFare(roomDetails.getBaseFare());
            room.setAcNonAc(roomDetails.getAcNonAc());
            room.setAvailable(roomDetails.getAvailable());
            room.setImageUrls(roomDetails.getImageUrls());
            // Hotel cannot be changed easily
            return roomRepository.save(room);
        }).orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + id));
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}
