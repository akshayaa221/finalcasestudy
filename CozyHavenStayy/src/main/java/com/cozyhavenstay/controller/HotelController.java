package com.cozyhavenstay.controller;

import com.cozyhavenstay.model.Hotel;
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
@RequestMapping("/api/v1/hotels")
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from your Angular frontend
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // Add a new hotel (HOTEL_OWNER or ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('HOTEL_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> addHotel(@RequestBody Hotel hotel) {
        try {
            // Further logic: Ensure the owner ID in the request body matches the authenticated user's ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) &&
                (hotel.getOwner() == null || !hotel.getOwner().getId().equals(authenticatedUserId))) {
                return new ResponseEntity<>("Forbidden: Hotel owner ID must match authenticated user ID.", HttpStatus.FORBIDDEN);
            }

            Hotel newHotel = hotelService.addHotel(hotel);
            return new ResponseEntity<>(newHotel, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add hotel: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all hotels (Publicly accessible, as defined in WebSecurityConfig)
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    // Get hotel by ID (Publicly accessible)
    @GetMapping("/{id}")
    public ResponseEntity<?> getHotelById(@PathVariable Long id) {
        Optional<Hotel> hotelOptional = hotelService.getHotelById(id);
        if (hotelOptional.isPresent()) {
            return new ResponseEntity<>(hotelOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Hotel not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    // Search hotels by location (Publicly accessible)
    @GetMapping("/search")
    public ResponseEntity<List<Hotel>> searchHotelsByLocation(@RequestParam String location) {
        List<Hotel> hotels = hotelService.searchHotelsByLocation(location);
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    // Get hotels by owner (HOTEL_OWNER can get their own, ADMIN can get any)
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and #ownerId == authentication.principal.id)")
    public ResponseEntity<List<Hotel>> getHotelsByOwner(@PathVariable Long ownerId) {
        List<Hotel> hotels = hotelService.getHotelsByOwner(ownerId);
        return new ResponseEntity<>(hotels, HttpStatus.OK);
    }

    // Update hotel (HOTEL_OWNER of that hotel or ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and (#hotelService.getHotelById(#id).isPresent() ? #hotelService.getHotelById(#id).get().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> updateHotel(@PathVariable Long id, @RequestBody Hotel hotelDetails) {
        // Ensure that the authenticated user is allowed to update this hotel
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // ADMIN can update any hotel
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER"))) {
            Optional<Hotel> existingHotel = hotelService.getHotelById(id);
            if (existingHotel.isEmpty() || !existingHotel.get().getOwner().getId().equals(authenticatedUserId)) {
                return new ResponseEntity<>("Forbidden: You do not own this hotel or hotel not found.", HttpStatus.FORBIDDEN);
            }
            // Also ensure the owner in hotelDetails is not being changed to a different user, or handle it as an admin-only change
            if (hotelDetails.getOwner() != null && hotelDetails.getOwner().getId() != null &&
                !hotelDetails.getOwner().getId().equals(existingHotel.get().getOwner().getId())) {
                return new ResponseEntity<>("Forbidden: Cannot change hotel owner.", HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to update hotels.", HttpStatus.FORBIDDEN);
        }

        try {
            Hotel updatedHotel = hotelService.updateHotel(id, hotelDetails);
            return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update hotel: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete hotel (ADMIN only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            return new ResponseEntity<>("Hotel with id " + id + " deleted successfully.", HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete hotel: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}