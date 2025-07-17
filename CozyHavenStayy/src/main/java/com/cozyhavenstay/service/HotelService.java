package com.cozyhavenstay.service;

import com.cozyhavenstay.model.Hotel;
import com.cozyhavenstay.model.User;
import com.cozyhavenstay.repository.HotelRepository;
import com.cozyhavenstay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public HotelService(HotelRepository hotelRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    public Hotel addHotel(Hotel hotel) {
        // Validate that the owner exists and has the HOTEL_OWNER or ADMIN role
        if (hotel.getOwner() == null || hotel.getOwner().getId() == null) {
            throw new IllegalArgumentException("Hotel must have an owner.");
        }
        User owner = userRepository.findById(hotel.getOwner().getId())
                                 .orElseThrow(() -> new IllegalArgumentException("Owner not found."));
        if (owner.getRole() != User.Role.HOTEL_OWNER && owner.getRole() != User.Role.ADMIN) { // Admin can also add hotels
            throw new IllegalArgumentException("User with ID " + owner.getId() + " is not a Hotel Owner or Admin.");
        }
        hotel.setOwner(owner); // Ensure the managed owner entity is set
        return hotelRepository.save(hotel);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> searchHotelsByLocation(String location) {
        return hotelRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Hotel> getHotelsByOwner(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId);
    }

    public Hotel updateHotel(Long id, Hotel hotelDetails) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setName(hotelDetails.getName());
            hotel.setLocation(hotelDetails.getLocation());
            hotel.setDescription(hotelDetails.getDescription());
            hotel.setAmenities(hotelDetails.getAmenities());
            // Owner change logic should be handled carefully, typically only by Admin
            if (hotelDetails.getOwner() != null && hotelDetails.getOwner().getId() != null) {
                User newOwner = userRepository.findById(hotelDetails.getOwner().getId())
                                              .orElseThrow(() -> new IllegalArgumentException("New owner not found."));
                if (newOwner.getRole() != User.Role.HOTEL_OWNER && newOwner.getRole() != User.Role.ADMIN) {
                    throw new IllegalArgumentException("New owner must be a Hotel Owner or Admin.");
                }
                hotel.setOwner(newOwner); // Update owner if provided and valid
            }
            return hotelRepository.save(hotel);
        }).orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + id));
    }

    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new IllegalArgumentException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
