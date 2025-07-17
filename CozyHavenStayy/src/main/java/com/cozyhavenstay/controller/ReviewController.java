package com.cozyhavenstay.controller;

import com.cozyhavenstay.model.Review;
import com.cozyhavenstay.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from your Angular frontend
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Submit a new review (GUEST only)
    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> submitReview(@RequestBody Review review) {
        // Ensure the user submitting the review is the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (review.getUser() == null || !review.getUser().getId().equals(authenticatedUserId)) {
            return new ResponseEntity<>("Forbidden: Cannot submit review as another user. User ID must match authenticated user.", HttpStatus.FORBIDDEN);
        }

        try {
            Review newReview = reviewService.submitReview(review);
            return new ResponseEntity<>(newReview, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) { // For cases like duplicate review, if you implement that logic
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to submit review: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get reviews by hotel (Publicly accessible, as defined in WebSecurityConfig)
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Review>> getReviewsByHotel(@PathVariable Long hotelId) { // No change needed here if it always returns List<Review>
        List<Review> reviews = reviewService.getReviewsByHotel(hotelId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    // Get reviews by user (GUEST can view their own, ADMIN can view any)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('GUEST') and #userId == authentication.principal.id)")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Long userId) { // Changed to ResponseEntity<?>
        // Additional in-method check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
            userId.equals(authenticatedUserId)) {
            List<Review> reviews = reviewService.getReviewsByUser(userId);
            return new ResponseEntity<>(reviews, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Forbidden: You can only view your own reviews.", HttpStatus.FORBIDDEN);
        }
    }

    // Get a single review by ID (Any authenticated user can view)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can view a specific review
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        Optional<Review> reviewOptional = reviewService.getReviewById(id);
        if (reviewOptional.isPresent()) {
            return new ResponseEntity<>(reviewOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Review not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    // Update review (Review owner or ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('GUEST') and (#reviewService.getReviewById(#id).isPresent() ? #reviewService.getReviewById(#id).get().getUser().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        // Additional in-method check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        Optional<Review> existingReviewOptional = reviewService.getReviewById(id);
        if (existingReviewOptional.isEmpty()) {
            return new ResponseEntity<>("Review not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        Review existingReview = existingReviewOptional.get();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isReviewOwner = existingReview.getUser().getId().equals(authenticatedUserId) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));

        if (isAdmin || isReviewOwner) {
            try {
                Review updatedReview = reviewService.updateReview(id, reviewDetails);
                return new ResponseEntity<>(updatedReview, HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to update review: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to update this review.", HttpStatus.FORBIDDEN);
        }
    }

    // Delete review (Review owner or ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('GUEST') and (#reviewService.getReviewById(#id).isPresent() ? #reviewService.getReviewById(#id).get().getUser().getId() == authentication.principal.id : false))")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        // Additional in-method check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        Optional<Review> existingReviewOptional = reviewService.getReviewById(id);
        if (existingReviewOptional.isEmpty()) {
            return new ResponseEntity<>("Review not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        Review existingReview = existingReviewOptional.get();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isReviewOwner = existingReview.getUser().getId().equals(authenticatedUserId) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));

        if (isAdmin || isReviewOwner) {
            try {
                reviewService.deleteReview(id);
                return new ResponseEntity<>("Review with id " + id + " deleted successfully.", HttpStatus.NO_CONTENT);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to delete review: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to delete this review.", HttpStatus.FORBIDDEN);
        }
    }
}