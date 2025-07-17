package com.cozyhavenstay.service;

import com.cozyhavenstay.model.Review;
import com.cozyhavenstay.model.Hotel;
import com.cozyhavenstay.model.User;
import com.cozyhavenstay.repository.ReviewRepository;
import com.cozyhavenstay.repository.HotelRepository;
import com.cozyhavenstay.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

   
    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, HotelRepository hotelRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
    }

    public Review submitReview(Review review) {
        // Validate User and Hotel existence
        User user = userRepository.findById(review.getUser().getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + review.getUser().getId()));
        Hotel hotel = hotelRepository.findById(review.getHotel().getId())
                                  .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + review.getHotel().getId()));

        // Basic validation for rating
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        // Prevent multiple reviews by the same user for the same hotel (optional, based on requirement)
        if (reviewRepository.findByUserIdAndHotelId(user.getId(), hotel.getId()) != null) {
            // You could throw an error or allow updating the existing review
            // For now, let's prevent duplicate
            // throw new IllegalStateException("User has already reviewed this hotel.");
        }


        review.setUser(user);
        review.setHotel(hotel);
        review.setReviewDate(LocalDate.now());

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByHotel(Long hotelId) {
        return reviewRepository.findByHotelIdOrderByReviewDateDesc(hotelId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdOrderByReviewDateDesc(userId);
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review updateReview(Long id, Review reviewDetails) {
        return reviewRepository.findById(id).map(review -> {
            review.setRating(reviewDetails.getRating());
            review.setComment(reviewDetails.getComment());
            review.setReviewDate(LocalDate.now()); // Update date on modification
            return reviewRepository.save(review);
        }).orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}