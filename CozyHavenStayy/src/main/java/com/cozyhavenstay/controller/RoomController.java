package com.cozyhavenstay.controller;

import com.cozyhavenstay.model.Room;
import com.cozyhavenstay.service.RoomService;
import com.cozyhavenstay.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cozyhavenstay.security.services.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin(origins = "http://localhost:4200")
public class RoomController {

    private final RoomService roomService;
    private final HotelService hotelService;

    public RoomController(RoomService roomService, HotelService hotelService) {
        this.roomService = roomService;
        this.hotelService = hotelService;
    }

    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and (#hotelService.getHotelById(#hotelId).isPresent() ? #hotelService.getHotelById(#hotelId).get().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> addRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // ADMIN can add room to any hotel
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER"))) {
            Optional<com.cozyhavenstay.model.Hotel> hotel = hotelService.getHotelById(hotelId);
            if (hotel.isEmpty() || !hotel.get().getOwner().getId().equals(authenticatedUserId)) {
                return new ResponseEntity<>("Forbidden: You do not own this hotel or hotel not found.", HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to add rooms.", HttpStatus.FORBIDDEN);
        }

        try {
            Room newRoom = roomService.addRoom(hotelId, room); // Pass hotelId to service
            return new ResponseEntity<>(newRoom, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only admin can get all rooms
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Optional<Room> roomOptional = roomService.getRoomById(id);
        if (roomOptional.isPresent()) {
            return new ResponseEntity<>(roomOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Room not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<Room> rooms = roomService.getRoomsByHotel(hotelId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and (#roomService.getRoomById(#id).isPresent() ? #roomService.getRoomById(#id).get().getHotel().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // ADMIN can update any room
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER"))) {
            Optional<Room> existingRoom = roomService.getRoomById(id);
            if (existingRoom.isEmpty() || !existingRoom.get().getHotel().getOwner().getId().equals(authenticatedUserId)) {
                return new ResponseEntity<>("Forbidden: You do not own this room or room not found.", HttpStatus.FORBIDDEN);
            }
            // Ensure hotel association is not changed to a different hotel
            if (roomDetails.getHotel() != null && roomDetails.getHotel().getId() != null &&
                !roomDetails.getHotel().getId().equals(existingRoom.get().getHotel().getId())) {
                return new ResponseEntity<>("Forbidden: Cannot change room's associated hotel.", HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to update rooms.", HttpStatus.FORBIDDEN);
        }

        try {
            Room updatedRoom = roomService.updateRoom(id, roomDetails);
            return new ResponseEntity<>(updatedRoom, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and (#roomService.getRoomById(#id).isPresent() ? #roomService.getRoomById(#id).get().getHotel().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // ADMIN can delete any room
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER"))) {
            Optional<Room> existingRoom = roomService.getRoomById(id);
            if (existingRoom.isEmpty() || !existingRoom.get().getHotel().getOwner().getId().equals(authenticatedUserId)) {
                return new ResponseEntity<>("Forbidden: You do not own this room or room not found.", HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to delete rooms.", HttpStatus.FORBIDDEN);
        }

        try {
            roomService.deleteRoom(id);
            return new ResponseEntity<>("Room with id " + id + " deleted successfully.", HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
