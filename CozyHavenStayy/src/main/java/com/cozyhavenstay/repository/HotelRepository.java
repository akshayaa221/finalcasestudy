package com.cozyhavenstay.repository;

import com.cozyhavenstay.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Custom query method: Find hotels by location
    List<Hotel> findByLocationContainingIgnoreCase(String location);

    // Custom query method: Find hotels by owner ID (useful for HotelOwner dashboard)
    List<Hotel> findByOwnerId(Long ownerId);

    // You might add more specific search methods later, e.g., by name, amenities etc.
    List<Hotel> findByNameContainingIgnoreCase(String name);
}