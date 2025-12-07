package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Review;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import com.example.web_based_vehicle_rental.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new review
     */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Review review = reviewService.createReview(
                    user,
                    request.getVehicleId(),
                    request.getRating(),
                    request.getComment(),
                    request.getReservationId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review submitted successfully");
            response.put("reviewId", review.getId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all reviews for a vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getVehicleReviews(@PathVariable Long vehicleId) {
        try {
            List<Review> reviews = reviewService.getVehicleReviews(vehicleId);
            ReviewService.ReviewStatistics stats = reviewService.getReviewStatistics(vehicleId);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get reviews by current user
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            List<Review> reviews = reviewService.getUserReviews(user.getId());
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            reviewService.deleteReview(reviewId, user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review deleted successfully"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Mark review as helpful
     */
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> markAsHelpful(@PathVariable Long reviewId) {
        try {
            reviewService.markAsHelpful(reviewId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Marked as helpful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Request DTO for creating reviews
     */
    public static class ReviewRequest {
        private Long vehicleId;
        private Integer rating;
        private String comment;
        private Long reservationId;

        // Getters and Setters
        public Long getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(Long vehicleId) {
            this.vehicleId = vehicleId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }
    }
}
