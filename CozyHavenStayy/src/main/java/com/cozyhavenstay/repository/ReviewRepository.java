package com.cozyhavenstay.repository;

import com.cozyhavenstay.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Custom query method: Find reviews by hotel ID
    List<Review> findByHotelIdOrderByReviewDateDesc(Long hotelId);

    // Custom query method: Find reviews by user ID
    List<Review> findByUserIdOrderByReviewDateDesc(Long userId);

    // Custom query method: Find a specific review by user and hotel
    Review findByUserIdAndHotelId(Long userId, Long hotelId);
}